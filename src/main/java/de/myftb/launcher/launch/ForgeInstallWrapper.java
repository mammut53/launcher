/*
 * MyFTBLauncher
 * Copyright (C) 2020 MyFTB <https://myftb.de>
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

package de.myftb.launcher.launch;

import de.myftb.launcher.Launcher;
import de.myftb.launcher.MavenHelper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.function.Predicate;

public class ForgeInstallWrapper extends MavenDownloadCallable {

    private ForgeInstallWrapper(String artifact, File targetFile) {
        super(artifact, targetFile);
    }

    public static ForgeInstallWrapper of(MavenHelper.MavenArtifact forgeArtifact) {
        return new ForgeInstallWrapper(forgeArtifact.getArtifactPath("installer"),
                new File(Launcher.getInstance().getSaveSubDirectory("libraries"), forgeArtifact.getFilePath("installer")));
    }

    @Override
    public File call() throws Exception {
        File installerFile = super.call();

        URLClassLoader installerClassLoader = new URLClassLoader(new URL[]{installerFile.toURI().toURL()});

        Class<?> classUtil = installerClassLoader.loadClass("net.minecraftforge.installer.json.Util");
        Method loadInstallProfile = classUtil.getDeclaredMethod("loadInstallProfile");
        Object installProfile = loadInstallProfile.invoke(null);;

        Class<?> classClientInstall = installerClassLoader.loadClass("net.minecraftforge.installer.actions.ClientInstall");
        Class<?> classInstall = installerClassLoader.loadClass("net.minecraftforge.installer.json.Install");
        Class<?> classProgressCallback = installerClassLoader.loadClass("net.minecraftforge.installer.actions.ProgressCallback");
        Constructor<?> clientInstallConstructor = classClientInstall.getDeclaredConstructor(classInstall, classProgressCallback);

        Field toStdOut = classProgressCallback.getDeclaredField("TO_STD_OUT");

        Object clientInstall = clientInstallConstructor.newInstance(installProfile, toStdOut.get(null));

        Method runMethod = classClientInstall.getDeclaredMethod("run", File.class, Predicate.class);
        Predicate<String> optionals = str -> true;
        File targetDir = Launcher.getInstance().getSaveDirectory();

        Files.write(new File(targetDir, "launcher_profiles.json").toPath(), "{}".getBytes());

        boolean success = (boolean) runMethod.invoke(clientInstall, targetDir, optionals);
        if (!success) {
            throw new IllegalStateException("Die Forge-Installation konnte nicht erfolgreich abgeschlossen werden");
        }

        return installerFile;
    }

}
