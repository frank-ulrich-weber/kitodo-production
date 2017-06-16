/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.serviceloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ServiceLoader;

import org.kitodo.config.ConfigMain;

public class KitodoServiceLoader<T> {
    private ServiceLoader<T> loader;
    private Class clazz;

    public KitodoServiceLoader(Class clazz) {
        this.clazz = clazz;
    }

    public T loadModule() throws IOException {

        loadModulesIntoClasspath();

        ServiceLoader<T> loader;
        loader = ServiceLoader.load(clazz);

        return loader.iterator().next();
    }

    public void loadModulesIntoClasspath() {
        Path pluginFolder = FileSystems.getDefault().getPath(ConfigMain.getParameter("pluginFolder"));

        URLClassLoader sysLoader = null;
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(pluginFolder, "*.jar");
            for (Path f : stream) {
                File loc = new File(f.toString());
                sysLoader = (URLClassLoader) this.getClass().getClassLoader();
                ArrayList<URL> urls = new ArrayList<>(Arrays.asList(sysLoader.getURLs()));
                URL udir = loc.toURI().toURL();

                if (!urls.contains(udir)) {
                    Class<URLClassLoader> sysClass = URLClassLoader.class;
                    Method method = sysClass.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(sysLoader, udir);
                }
            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
