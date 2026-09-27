package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

/**
 * Small Cobblemon-facing compatibility boundary for aggression state.
 *
 * Fight or Flight also calls the inherited target APIs, so this class does not
 * reference that optional mod or inject into Cobblemon's Brain construction.
 * If Cobblemon 1.8 changes the required memories, this is the primary class to
 * adapt.
 */
public final class CobblemonAggressionBridge {
    private static final float CHASE_SPEED = 1.0F;
    private static final int CLOSE_ENOUGH_DISTANCE = 1;

    private CobblemonAggressionBridge() {
    }

    public static void setTarget(
            PokemonEntity pokemon,
            LivingEntity target
    ) {
        pokemon.setTarget(target);

        Brain<?> brain = pokemon.getBrain();

        if (brain.checkMemory(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
        }

        if (brain.checkMemory(
                MemoryModuleType.ANGRY_AT,
                MemoryStatus.REGISTERED
        )) {
            brain.setMemory(MemoryModuleType.ANGRY_AT, target.getUUID());
        }

        updatePursuitMemory(pokemon, target, brain);
    }

    /**
     * Cobblemon 1.7.x combat uses Brain memories: ATTACK_TARGET identifies the
     * enemy, while WALK_TARGET supplies movement. Once the Pokémon is close
     * enough to attack, WALK_TARGET must be absent so Cobblemon's AttackTask
     * can run.
     */
    private static void updatePursuitMemory(
            PokemonEntity pokemon,
            LivingEntity target,
            Brain<?> brain
    ) {
        if (brain.checkMemory(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            brain.setMemory(
                    MemoryModuleType.LOOK_TARGET,
                    new EntityTracker(target, true)
            );
        }

        if (!brain.checkMemory(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            return;
        }

        if (pokemon.isWithinMeleeAttackRange(target)) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            return;
        }

        brain.setMemory(
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(
                        new EntityTracker(target, false),
                        CHASE_SPEED,
                        CLOSE_ENOUGH_DISTANCE
                )
        );
    }

    public static void clearTarget(PokemonEntity pokemon) {
        pokemon.setTarget(null);

        Brain<?> brain = pokemon.getBrain();

        if (brain.checkMemory(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }

        if (brain.checkMemory(
                MemoryModuleType.ANGRY_AT,
                MemoryStatus.REGISTERED
        )) {
            brain.eraseMemory(MemoryModuleType.ANGRY_AT);
        }

        if (brain.checkMemory(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        }

        if (brain.checkMemory(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
    }
}
