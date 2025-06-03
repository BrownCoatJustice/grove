/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.habism.grove.util;

/**
 *
 * @author habism
 */
public final class Config {

    private static final Config instance = new Config();

    private boolean inDebugMode;
    private boolean inCliMode;

    private Config() {
    }

    public static Config getInstance() {
        return instance;
    }

    public boolean isInDebugMode() {
        return inDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.inDebugMode = debugMode;
    }

    public boolean isInCliMode() {
        return inCliMode;
    }

    public void setCliMode(boolean cliMode) {
        this.inCliMode = cliMode;
    }
}
