package util

import grails.util.Holders
import liquibase.FileOpener;
import liquibase.ClassLoaderFileOpener;

import grails.util.GrailsUtil
import org.grails.io.support.GrailsResourceUtils;

class GrailsFileOpener implements FileOpener {

    def fileOpener;

    public GrailsFileOpener() {
        if (Holders.grailsApplication.isWarDeployed()) {
            fileOpener = new ClassLoaderFileOpener();
        } else {
            fileOpener = new DevFileOpener();
        }
    }

    InputStream getResourceAsStream(String file) throws IOException {
        fileOpener.getResourceAsStream(file);
    }

    Enumeration<URL> getResources(String packageName) throws IOException {
        fileOpener.getResources(packageName);
    }

    ClassLoader toClassLoader() {
        fileOpener.toClassLoader();
    }

}

class DevFileOpener implements FileOpener {

    InputStream getResourceAsStream(String file) throws IOException {
        if (GrailsUtil.grailsVersion.startsWith('1.0')) {
            return getClass().getClassLoader().getResourceAsStream("grails-app/migrations/"+file)
        } else {
            return new FileInputStream(new File(GrailsResourceUtils.GRAILS_APP_DIR + "/migrations/" + file))
        }
    }

    Enumeration<URL> getResources(String packageName) throws IOException {
        getClass().classLoader.getResources("grails-app/migrations/"+packageName)
    }

    public ClassLoader toClassLoader() {
        getClass().classLoader
    }

}