package net.pcal.quicksort;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.pcal.quicksort.QuicksortConfig.QuicksortChestConfig;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

class QuicksortConfigParser {

    static QuicksortConfig parse(final InputStream in, QuicksortChestConfig defaultChestConfig) throws IOException {
        final List<QuicksortChestConfig> chests = new ArrayList<>();
        final String rawJson = stripComments(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        final Gson gson = new Gson();

        final QuicksortConfigGson configGson = gson.fromJson(rawJson, QuicksortConfigGson.class);
        for (QuicksortChestConfigGson chestGson : configGson.quicksortChests) {
            chests.add(defaultChestConfig = createWithDefaults(defaultChestConfig,
                    chestGson.chestName,
                    chestGson.baseBlockId,
                    chestGson.range,
                    chestGson.cooldownTicks,
                    chestGson.animationTicks,
                    chestGson.soundVolume,
                    chestGson.soundPitch,
                    chestGson.nbtMatchEnabledIds != null ? chestGson.nbtMatchEnabledIds : chestGson.enchantmentMatchingIds,
                    chestGson.targetContainerIds));
        }
        // adjust logging to configured level
        final String configuredLevel = configGson.logLevel;
        final Level logLevel = Level.getLevel(configuredLevel);
        if (logLevel == null) throw new IllegalArgumentException("Invalid logLevel " + configuredLevel);
        return new QuicksortConfig(Collections.unmodifiableList(chests), logLevel);
    }

    static QuicksortChestConfig createWithDefaults(
            QuicksortChestConfig dflt,
            String chestName,
            String baseBlockId,
            Integer range,
            Integer cooldownTicks,
            Integer animationTicks,
            Float soundVolume,
            Float soundPitch,
            Collection<String> enchantmentMatchingIds,
            Collection<String> targetContainerIds) {

        // First, check if chestName is provided, if so, use it.
        String finalChestName = (chestName != null && !chestName.isEmpty()) ? chestName : (dflt == null ? null : dflt.chestName());

        // If chestName is not provided, fallback to baseBlockId, and convert it to ResourceLocation
        ResourceLocation finalBaseBlockId = (finalChestName == null && baseBlockId != null && !baseBlockId.isEmpty())
                ? ResourceLocation.parse(baseBlockId)
                : (dflt == null ? null : dflt.baseBlockId());

        return new QuicksortChestConfig(
                //ResourceLocation.parse(requireNonNull(baseBlockId, "baseBlockId is required")),
                finalChestName,  // Use chestName if available
                finalBaseBlockId != null ? ResourceLocation.parse(requireNonNull(baseBlockId, "baseBlockId is required")) : null,  // Only use baseBlockId if chestName is not present
                requireNonNull(range != null ? range : dflt == null ? null : dflt.range(),
                        "range is required"),
                requireNonNull(cooldownTicks != null ? cooldownTicks : dflt == null ? null : dflt.cooldownTicks(),
                        "cooldownTicks is required"),
                requireNonNull(animationTicks != null ? animationTicks : dflt == null ? null : dflt.animationTicks(),
                        "animationTicks is required"),
                requireNonNull(soundVolume != null ? soundVolume : dflt == null ? null : dflt.soundVolume(),
                        "soundVolume is required"),
                requireNonNull(soundPitch != null ? soundPitch : dflt == null ? null : dflt.soundPitch(),
                        "soundPitch is required"),
                requireNonNull(enchantmentMatchingIds != null ? toIdentifierSet(enchantmentMatchingIds) : dflt == null ? null : dflt.enchantmentMatchingIds(),
                        "enchantmentMatchingIds"),
                requireNonNull(targetContainerIds != null ? toIdentifierSet(targetContainerIds) : dflt == null ? null : dflt.targetContainerIds(),
                        "targetContainerIds")
        );
    }

    private static Set<ResourceLocation> toIdentifierSet(Collection<String> enchantmentMatchingIds) {
        final Set<ResourceLocation> set = new HashSet<>();
        for (String id : enchantmentMatchingIds) set.add(ResourceLocation.parse(id));
        return set;
    }

    // ===================================================================================
    // Private methods

    private static String stripComments(String json) throws IOException {
        final StringBuilder out = new StringBuilder();
        final BufferedReader br = new BufferedReader(new StringReader(json));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.strip().startsWith(("//"))) out.append(line).append('\n');
        }
        return out.toString();
    }

    // ===================================================================================
    // Gson object model

    public static class QuicksortConfigGson {
        List<QuicksortChestConfigGson> quicksortChests;
        String logLevel;
    }

    public static class QuicksortChestConfigGson {
        String chestName;
        String baseBlockId;
        Integer range;
        Integer cooldownTicks;
        Integer animationTicks;
        Float soundVolume;
        Float soundPitch;
        List<String> enchantmentMatchingIds;
        List<String> targetContainerIds;

        @Deprecated // this was the old name for enchantmentMatchingIds
        List<String> nbtMatchEnabledIds;
    }
}
