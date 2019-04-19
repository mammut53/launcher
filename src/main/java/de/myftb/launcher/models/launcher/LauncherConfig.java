/*
 * MyFTBLauncher
 * Copyright (C) 2019 MyFTB <https://myftb.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.myftb.launcher.models.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

public class LauncherConfig {

    private final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(UserAuthentication.class, new UserAuthenticationSerializer(this))
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    @Expose private String clientToken = UUID.randomUUID().toString();
    @Expose private String jvmArgs = "";

    @Expose private boolean metricsEnabled = true;
    @Expose private String metricsToken = UUID.randomUUID().toString();

    @Expose private int minMemory = 1024;
    @Expose private int maxMemory = 1024;

    @Expose private int gameWidth = 854;
    @Expose private int gameHeight = 480;

    @Expose private String packKey = "";
    @Expose private String installationDir;

    @Expose private UserAuthentication profile = null;

    public String getClientToken() {
        return this.clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getJvmArgs() {
        return this.jvmArgs;
    }

    public void setJvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public boolean isMetricsEnabled() {
        return this.metricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public String getMetricsToken() {
        return this.metricsToken;
    }

    public void setMetricsToken(String metricsToken) {
        this.metricsToken = metricsToken;
    }

    public int getMinMemory() {
        return this.minMemory;
    }

    public void setMinMemory(int minMemory) {
        this.minMemory = minMemory;
    }

    public int getMaxMemory() {
        return this.maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getGameWidth() {
        return this.gameWidth;
    }

    public void setGameWidth(int gameWidth) {
        this.gameWidth = gameWidth;
    }

    public int getGameHeight() {
        return this.gameHeight;
    }

    public void setGameHeight(int gameHeight) {
        this.gameHeight = gameHeight;
    }

    public String getPackKey() {
        return this.packKey;
    }

    public void setPackKey(String packKey) {
        this.packKey = packKey;
    }

    public String getInstallationDir() {
        return this.installationDir;
    }

    public void setInstallationDir(String installationDir) {
        this.installationDir = installationDir;
    }

    public void setProfile(UserAuthentication profile) {
        this.profile = profile;
    }

    public UserAuthentication getProfile() {
        return this.profile;
    }

    public AuthenticationService getAuthenticationService() {
        return new YggdrasilAuthenticationService(Proxy.NO_PROXY, this.getClientToken());
    }

    public void save(File dir) throws IOException {
        File configFile = new File(dir, "config.json");
        Files.write(configFile.toPath(), this.gson.toJson(this, LauncherConfig.class).getBytes(StandardCharsets.UTF_8));
    }

    public JsonObject toJson() {
        return this.gson.toJsonTree(this).getAsJsonObject();
    }

    public LauncherConfig readConfig(JsonObject jsonObject) {
        return this.gson.fromJson(jsonObject, LauncherConfig.class);
    }

    public LauncherConfig readConfig(File dir) throws IOException {
        File configFile = new File(dir, "config.json");
        if (configFile.isFile()) {
            return this.gson.fromJson(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8), LauncherConfig.class);
        }

        return this;
    }

}