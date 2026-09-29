package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Small Cobblemon-facing compatibility boundary for aggression state.
 *
 * <p>The 1.8.1 compatibility hotfix intentionally keeps this bridge at the
 * established public behavior level: the inherited entity target plus
 * ATTACK_TARGET and ANGRY_AT memories. Experimental manual LOOK_TARGET /
 * WALK_TARGET pursuit assistance remains deferred to the later AI update.</p>
 *
 * <p>Fight or Flight also calls the inherited target APIs, so this class does
 * not reference that optional mod or inject into Cobblemon's Brain
 * construction.</p>
 */
public final class CobblemonAggressionBridge {
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
    }
}
