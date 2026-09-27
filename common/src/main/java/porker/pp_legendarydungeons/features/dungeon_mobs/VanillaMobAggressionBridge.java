package porker.pp_legendarydungeons.features.dungeon_mobs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Pillager;
import porker.pp_legendarydungeons.features.dungeon_factions.DungeonFactionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Small compatibility boundary around vanilla mob targeting.
 *
 * <p>The encounter system chooses a target but intentionally does not replace
 * the mob's attack goals. Pillagers still use their crossbow logic, drowned
 * still use their native melee/trident logic, and guardians still use their
 * native attack goals. A manager-assigned target is retained between bounded
 * manager updates so a native target selector cannot erase it immediately.</p>
 */
public final class VanillaMobAggressionBridge {
    private static final Map<UUID, UUID> RETAINED_TARGETS = new HashMap<>();

    private static final double CHASE_ASSIST_SPEED = 1.0D;
    private static final double MELEE_STOP_DISTANCE = 2.0D;
    private static final double RANGED_STOP_DISTANCE = 6.0D;
    private static final double NAVIGATION_TARGET_TOLERANCE = 2.0D;

    private VanillaMobAggressionBridge() {
    }

    public static void setTarget(Mob mob, LivingEntity target) {
        if (mob == null) {
            return;
        }

        if (target == null) {
            clearTarget(mob);
            return;
        }

        RETAINED_TARGETS.put(mob.getUUID(), target.getUUID());
        mob.setTarget(target);
        assistPursuit(mob, target);
    }

    /**
     * Gives managed targets enough movement authority to beat ordinary wander
     * paths without installing another Goal that would compete with the mob's
     * native melee, crossbow, trident, or beam attack Goal.
     */
    private static void assistPursuit(Mob mob, LivingEntity target) {
        if (mob.isNoAi()
                || !target.isAlive()
                || mob.level() != target.level()) {
            return;
        }

        double stopDistance = (mob instanceof Pillager || mob instanceof Guardian)
                ? RANGED_STOP_DISTANCE
                : MELEE_STOP_DISTANCE;

        if (mob.distanceToSqr(target) <= stopDistance * stopDistance) {
            return;
        }

        PathNavigation navigation = mob.getNavigation();
        if (navigationAlreadyTracksTarget(navigation, target)) {
            return;
        }

        navigation.moveTo(target, CHASE_ASSIST_SPEED);
    }

    private static boolean navigationAlreadyTracksTarget(
            PathNavigation navigation,
            LivingEntity target
    ) {
        if (navigation.isDone() || navigation.getTargetPos() == null) {
            return false;
        }

        double dx = navigation.getTargetPos().getX() + 0.5D - target.getX();
        double dy = navigation.getTargetPos().getY() + 0.5D - target.getY();
        double dz = navigation.getTargetPos().getZ() + 0.5D - target.getZ();
        double toleranceSquared =
                NAVIGATION_TARGET_TOLERANCE * NAVIGATION_TARGET_TOLERANCE;

        return dx * dx + dy * dy + dz * dz <= toleranceSquared;
    }

    public static void clearTarget(Mob mob) {
        if (mob == null) {
            return;
        }

        RETAINED_TARGETS.remove(mob.getUUID());
        mob.setTarget(null);
    }

    /**
     * Called from the Mob mixin before vanilla changes a managed mob's target.
     *
     * <p>Only two cases are blocked:
     * <ul>
     *   <li>vanilla tries to clear the exact live target currently retained by
     *       the dungeon manager;</li>
     *   <li>a managed mob tries to select another managed member of its own
     *       faction.</li>
     * </ul>
     * Other native target replacements remain allowed.</p>
     */
    public static boolean shouldCancelTargetChange(
            Mob mob,
            LivingEntity requestedTarget
    ) {
        if (mob == null) {
            return false;
        }

        if (requestedTarget != null) {
            return DungeonMobManager.getRecord(mob.getUUID()).isPresent()
                    && DungeonFactionService.areAllies(mob, requestedTarget);
        }

        UUID retainedTarget = RETAINED_TARGETS.get(mob.getUUID());
        LivingEntity currentTarget = mob.getTarget();

        return retainedTarget != null
                && currentTarget != null
                && currentTarget.isAlive()
                && retainedTarget.equals(currentTarget.getUUID());
    }

    public static void forget(UUID mobUuid) {
        if (mobUuid != null) {
            RETAINED_TARGETS.remove(mobUuid);
        }
    }

    public static void clearAll() {
        RETAINED_TARGETS.clear();
    }
}
