package com.blocklaunch.blwarps.managers;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.Warp;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlatFileManager extends StorageManager {

    private ObjectMapper mapper = new ObjectMapper();

    private static final String ERROR_FILE_WRITE = "There was an error writing to the file!";
    private static final String ERROR_FILE_READ = "There was an error reading the warps file!";
    
    private File warpsFile;

    public FlatFileManager(File warpsFile, BLWarps plugin) {
        super(plugin);
        this.warpsFile = warpsFile;
    }

    /**
     * Reads in warps file, and de-serializes it to a List<Warp>
     */
    @Override
    public void loadWarps() {
        Optional<List<Warp>> optWarps = readInWarps();
        if (!optWarps.isPresent()) {
            return;
        }

        WarpManager.warps = optWarps.get();
    }

    /**
     * Read in warp file and deserialize to List<Warp> Insert new warp to List Serialize to JSON and
     * write to file
     * 
     * @param warp The new warp to save
     */
    @Override
    public void saveNewWarp(Warp warp) {
        Optional<List<Warp>> warpsOpt = readInWarps();

        List<Warp> currentlySavedWarps = new ArrayList<Warp>();
        if (warpsOpt.isPresent()) {
            currentlySavedWarps = warpsOpt.get();
        }
        currentlySavedWarps.add(warp);

        writeOutWarps(currentlySavedWarps);
    }

    /**
     * Read in warp file and deserialize to List<Warp>. Remove the warp from the list Serialize to
     * JSON and write to file
     * 
     * @param warp The warp to remove
     */
    @Override
    public void deleteWarp(Warp warp) {
        Optional<List<Warp>> warpsOpt = readInWarps();

        if (!warpsOpt.isPresent()) {
            return;
        }
        List<Warp> warps = warpsOpt.get();

        // Temporary warp for avoiding ConcurrentModificationException
        Warp warpToRemove = null;
        for (Warp w : warps) {
            if (w.getName().equalsIgnoreCase(warp.getName())) {
                warpToRemove = w;
            }
        }
        if (warpToRemove != null)
            warps.remove(warpToRemove);

        writeOutWarps(warps);
    }

    /**
     * Find the saved warp with the same name Remove the saved warp Add the new, updated warp
     * 
     * @param warp The warp to update
     */
    @Override
    public void updateWarp(Warp warp) {
        Optional<List<Warp>> warpsOpt = readInWarps();

        if (!warpsOpt.isPresent()) {
            return;
        }
        List<Warp> warps = warpsOpt.get();

        // Temporary warp for avoiding ConcurrentModificationException
        Warp warpToUpdate = null;
        for (Warp w : warps) {
            if (w.getName().equalsIgnoreCase(warp.getName())) {
                warpToUpdate = w;
            }
        }
        if (warpToUpdate != null) {
            warps.remove(warpToUpdate);
            warps.add(warp);
        }
        writeOutWarps(warps);

    }

    /**
     * Read in warp file (if it exists) and serialize to a List<Warp> (if possible)
     * 
     * @return An Optional containing the List<Warp>, or Optional.absent() otherwise
     */
    private Optional<List<Warp>> readInWarps() {
        if (!warpsFile.exists()) {
            return Optional.absent();
        }

        try {
            List<Warp> warps = mapper.readValue(warpsFile, new TypeReference<List<Warp>>() {});
            return Optional.of(warps);
        } catch (IOException e) {
            plugin.getLogger().warn(ERROR_FILE_READ);
            e.printStackTrace();
            return Optional.absent();
        }

    }

    /**
     * Serializes a List<Warp> and saves it to the file.
     * 
     * @param warps The warps to save to the file
     * @return The success of the saving operation
     */
    private void writeOutWarps(List<Warp> warps) {
        try {
            warpsFile.createNewFile(); // Only creates the file if it doesn't already exist.
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(warpsFile, warps);
        } catch (IOException e) {
            plugin.getLogger().warn(ERROR_FILE_WRITE);
            e.printStackTrace();
        }
    }

}
