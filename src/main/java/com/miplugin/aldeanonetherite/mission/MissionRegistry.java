package com.miplugin.aldeanonetherite.mission;

import com.miplugin.aldeanonetherite.AldeanoNetheritePlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Lista de las 50 misiones: 15 media-alta, 25 alta y 10 muy alta. */
public final class MissionRegistry {

    private final AldeanoNetheritePlugin plugin;
    private final Map<Integer, Mission> missions = new LinkedHashMap<>();
    private int nextId = 1;

    public MissionRegistry(AldeanoNetheritePlugin plugin) {
        this.plugin = plugin;
    }

    public Mission get(int id) {
        return missions.get(id);
    }

    public Collection<Mission> all() {
        return missions.values();
    }

    public void load() {
        missions.clear();
        nextId = 1;
        registerMediaAlta();
        registerAlta();
        registerMuyAlta();

        long ma = missions.values().stream().filter(m -> m.difficulty() == Difficulty.MEDIA_ALTA).count();
        long a = missions.values().stream().filter(m -> m.difficulty() == Difficulty.ALTA).count();
        long mua = missions.values().stream().filter(m -> m.difficulty() == Difficulty.MUY_ALTA).count();
        plugin.getLogger().info("Misiones cargadas: " + missions.size()
                + " (media-alta: " + ma + ", alta: " + a + ", muy alta: " + mua + ")");
    }

    // ---------------------------------------------------------------- 15 media-alta
    private void registerMediaAlta() {
        Difficulty d = Difficulty.MEDIA_ALTA;
        add(d, MissionType.KILL, "Cazador de Zombis", "Elimina %d zombis.", 40, "ZOMBIE");
        add(d, MissionType.KILL, "Francotirador Esquelético", "Elimina %d esqueletos.", 35, "SKELETON");
        add(d, MissionType.KILL, "Plaga de Arañas", "Elimina %d arañas (normales o de cueva).", 30, "SPIDER", "CAVE_SPIDER");
        add(d, MissionType.KILL, "Purga de Creepers", "Elimina %d creepers.", 25, "CREEPER");
        add(d, MissionType.KILL, "Cazador de Brujas", "Elimina %d brujas.", 8, "WITCH");
        add(d, MissionType.MINE, "Minero de Hierro", "Mina %d minerales de hierro.", 80, "IRON_ORE", "DEEPSLATE_IRON_ORE");
        add(d, MissionType.MINE, "Fiebre del Oro", "Mina %d minerales de oro.", 50, "GOLD_ORE", "DEEPSLATE_GOLD_ORE", "NETHER_GOLD_ORE");
        add(d, MissionType.MINE, "Buscador de Redstone", "Mina %d minerales de redstone.", 100, "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE");
        add(d, MissionType.MINE, "Lapidario", "Mina %d minerales de lapislázuli.", 60, "LAPIS_ORE", "DEEPSLATE_LAPIS_ORE");
        add(d, MissionType.FISH, "Pescador Experto", "Pesca %d objetos.", 30, "ANY");
        add(d, MissionType.BREED, "Ganadero", "Cría %d animales de granja (vacas, ovejas, cerdos o gallinas).", 20, "COW", "SHEEP", "PIG", "CHICKEN");
        add(d, MissionType.BREED, "Criador de Monturas", "Cría %d caballos, burros, llamas o camellos.", 6, "HORSE", "DONKEY", "LLAMA", "CAMEL");
        add(d, MissionType.CRAFT, "Hechicero", "Fabrica %d mesas de encantamientos.", 3, "ENCHANTING_TABLE");
        add(d, MissionType.CRAFT, "Herrero Maestro", "Fabrica %d yunques.", 3, "ANVIL");
        add(d, MissionType.CRAFT, "Piromaníaco", "Fabrica %d bloques de TNT.", 10, "TNT");
    }

    // ---------------------------------------------------------------- 25 alta
    private void registerAlta() {
        Difficulty d = Difficulty.ALTA;
        add(d, MissionType.KILL, "Rey del Nether", "Elimina %d piglins zombificados.", 25, "ZOMBIFIED_PIGLIN");
        add(d, MissionType.KILL, "Cazador de Blazes", "Elimina %d blazes.", 20, "BLAZE");
        add(d, MissionType.KILL, "Sombra Marchita", "Elimina %d esqueletos wither.", 15, "WITHER_SKELETON");
        add(d, MissionType.KILL, "Lágrimas de Ghast", "Elimina %d ghasts.", 8, "GHAST");
        add(d, MissionType.KILL, "Pesadilla de Magma", "Elimina %d cubos de magma.", 30, "MAGMA_CUBE");
        add(d, MissionType.KILL, "Domador de Hoglins", "Elimina %d hoglins.", 15, "HOGLIN");
        add(d, MissionType.KILL, "Cazador de Piglins", "Elimina %d piglins o piglins brutos.", 25, "PIGLIN", "PIGLIN_BRUTE");
        add(d, MissionType.KILL, "Terror Nocturno", "Elimina %d phantoms.", 25, "PHANTOM");
        add(d, MissionType.KILL, "Merodeador del Fin", "Elimina %d endermans.", 40, "ENDERMAN");
        add(d, MissionType.KILL, "Azote del Mar", "Elimina %d ahogados.", 30, "DROWNED");
        add(d, MissionType.KILL, "Cazador de Saqueadores", "Elimina %d saqueadores o vindicadores.", 30, "PILLAGER", "VINDICATOR");
        add(d, MissionType.KILL, "Exorcista", "Elimina %d evocadores.", 5, "EVOKER");
        add(d, MissionType.KILL, "Domador de Ravagers", "Elimina %d ravagers.", 3, "RAVAGER");
        add(d, MissionType.KILL, "Guardián Profundo", "Elimina %d guardianes.", 20, "GUARDIAN");
        add(d, MissionType.KILL, "Frío Mortal", "Elimina %d vagabundos o pantanosos (stray y bogged).", 30, "STRAY", "BOGGED");
        add(d, MissionType.MINE, "Excavador de Diamantes", "Mina %d minerales de diamante.", 40, "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE");
        add(d, MissionType.MINE, "Cazador de Esmeraldas", "Mina %d minerales de esmeralda.", 20, "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE");
        add(d, MissionType.MINE, "Minero del Nether", "Mina %d restos antiguos.", 15, "ANCIENT_DEBRIS");
        add(d, MissionType.MINE, "Cuarzo Infernal", "Mina %d minerales de cuarzo del Nether.", 150, "NETHER_QUARTZ_ORE");
        add(d, MissionType.MINE, "Roca Volcánica", "Mina %d bloques de obsidiana.", 40, "OBSIDIAN");
        add(d, MissionType.MINE, "Fiebre del Cobre", "Mina %d minerales de cobre.", 200, "COPPER_ORE", "DEEPSLATE_COPPER_ORE");
        add(d, MissionType.FISH, "Pescador Legendario", "Pesca %d objetos.", 60, "ANY");
        add(d, MissionType.CRAFT, "Arquitecto del Fin", "Fabrica %d cristales del End.", 3, "END_CRYSTAL");
        add(d, MissionType.CRAFT, "Ancla del Nether", "Fabrica %d anclas de reaparición.", 2, "RESPAWN_ANCHOR");
        add(d, MissionType.CRAFT, "Maestro de Cofres", "Fabrica %d cofres del End.", 5, "ENDER_CHEST");
    }

    // ---------------------------------------------------------------- 10 muy alta
    private void registerMuyAlta() {
        Difficulty d = Difficulty.MUY_ALTA;
        add(d, MissionType.KILL, "Silencio Profundo", "Elimina %d warden.", 1, "WARDEN");
        add(d, MissionType.KILL, "Verdugo del Wither", "Elimina %d wither.", 1, "WITHER");
        add(d, MissionType.KILL, "Verdugo Draconiano", "Elimina %d dragón del End.", 1, "ENDER_DRAGON");
        add(d, MissionType.KILL, "Ancestro del Océano", "Elimina %d guardianes ancestrales.", 2, "ELDER_GUARDIAN");
        add(d, MissionType.KILL, "Cazador de Shulkers", "Elimina %d shulkers.", 12, "SHULKER");
        add(d, MissionType.KILL, "Exterminador", "Elimina %d monstruos hostiles de cualquier tipo.", 150, "HOSTILE");
        add(d, MissionType.CRAFT, "Maestro de Faros", "Fabrica %d faros.", 2, "BEACON");
        add(d, MissionType.MINE, "Minero Extremo", "Mina %d restos antiguos.", 40, "ANCIENT_DEBRIS");
        add(d, MissionType.MINE, "Fiebre del Diamante", "Mina %d minerales de diamante.", 128, "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE");
        add(d, MissionType.FISH, "Pescador Supremo", "Pesca %d objetos.", 150, "ANY");
    }

    // ---------------------------------------------------------------- utilidades
    private void add(Difficulty difficulty, MissionType type, String name, String desc, int amount, String... targets) {
        int id = nextId++; // el id depende de la posición, así no cambia si una misión falla
        Set<String> valid = new LinkedHashSet<>();
        for (String raw : targets) {
            String key = raw.toUpperCase(Locale.ROOT);
            if (isValid(type, key)) {
                valid.add(key);
            } else {
                plugin.getLogger().warning("Objetivo inválido '" + raw + "' en la misión '" + name + "'");
            }
        }
        if (valid.isEmpty()) {
            plugin.getLogger().warning("La misión '" + name + "' se omitió porque no tiene objetivos válidos.");
            return;
        }
        missions.put(id, new Mission(id, name, String.format(desc, amount), difficulty, type, valid, amount));
    }

    private boolean isValid(MissionType type, String key) {
        if (type == MissionType.KILL || type == MissionType.BREED) {
            if (key.equals("HOSTILE")) {
                return type == MissionType.KILL;
            }
            try {
                EntityType.valueOf(key);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        if (type == MissionType.MINE || type == MissionType.CRAFT) {
            return Material.matchMaterial(key) != null;
        }
        return key.equals("ANY");
    }
}
