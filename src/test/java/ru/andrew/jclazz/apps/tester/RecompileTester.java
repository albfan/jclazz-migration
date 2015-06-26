package ru.andrew.jclazz.apps.tester;

import ru.andrew.jclazz.*;
import ru.andrew.jclazz.apps.*;
import ru.andrew.jclazz.apps.decomp.*;

import java.io.*;
import java.util.*;

public class RecompileTester implements Tester, FileFilter
{
    private String javac;
    private boolean isPrecompile;
    private String buildDir;
    private String decompileDir;
    private String testDir;
    private String recompileDir;

    public static final String BUILD_DIR = "../target/build";
    public static final String RECOMPILE_DIR = "../target/recompiled";
    public static final String DECOMPILE_DIR = "../target/decompiled";

    String fs = System.getProperty("file.separator");

    public RecompileTester(Map params)
    {
        isPrecompile = "yes".equals(params.get(Params.RCT_PRECOMPILE));

        String javaHome = (String) params.get(Params.RCT_JAVA_HOME);
        if (javaHome == null || "".equals(javaHome))
        {
            throw new RuntimeException("No " + Params.RCT_JAVA_HOME + " specified");
        }
        javac = javaHome + fs + "bin" + fs + "javac";

        testDir = (String) params.get(Params.TESTS_DIR);

        if (params.get(Params.RCT_JAVAC_ARGS) != null)
        {
            this.javac += " " + params.get(Params.RCT_JAVAC_ARGS);
        }

        // Creating build directory
        buildDir = buildPath(BUILD_DIR);
        decompileDir = buildPath(DECOMPILE_DIR);
        recompileDir = buildPath(RECOMPILE_DIR);

    }

    private String buildPath(String dir) {
        File bdir = new File(testDir + fs + dir);
        deleteRecursively(bdir);
        bdir.mkdir();
        return bdir.getAbsolutePath();
    }

    public void deleteRecursively(String path) {
        File file = new File(path);
        deleteRecursively(file);
    }

    private void deleteRecursively(File file) {
        File[] currList;
        Stack<File> stack = new Stack<File>();
        stack.push(file);
        while (! stack.isEmpty()) {
            if (stack.lastElement().isDirectory()) {
                currList = stack.lastElement().listFiles();
                if (currList.length > 0) {
                    for (File curr: currList) {
                        stack.push(curr);
                    }
                } else {
                    stack.pop().delete();
                }
            } else {
                stack.pop().delete();
            }
        }
    }

    public String getName()
    {
        return "recompile";
    }

    public boolean test(File file)
    {
        try
        {
            Map params = new HashMap();
            params.put(Params.EXTENSION, "java");
                    
            String fileAbsolutePath = file.getAbsolutePath();
            String fileWithoutExtension = fileAbsolutePath.substring(0, fileAbsolutePath.lastIndexOf('.'));

            if (isPrecompile)
            {
                boolean res = execJavac(javac + " -sourcepath " + testDir + " -verbose -d " +buildDir, fileAbsolutePath);
                if (!res) {
                    return false;
                }
            }

            int lasfIndexFs = fileWithoutExtension.lastIndexOf(fs);
            String originalPath = fileWithoutExtension.substring(0, lasfIndexFs);
            File originalDir = new File(originalPath);

            String clname = fileWithoutExtension.substring(lasfIndexFs + 1);

            // Calculating destination directory
            String packageDir = originalDir.getAbsolutePath();
            packageDir = packageDir.substring(testDir.length()+1);


            params.put(Params.OUT_FILE, decompileDir + fs + packageDir + fs + clname + ".java");

            String outFile = ClassDecompiler.generateJavaFile(buildDir + fs + packageDir + fs + clname + ".class", params);

            boolean res = execJavac(javac + " -sourcepath " + decompileDir + " -verbose -d " +recompileDir, outFile);

            if (!res) {
                return false;
            }

            return compare(fileWithoutExtension);
        }
        catch (ClazzException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean execJavac(String cmd, String path) throws IOException
    {
        JavacWrap wrapper = new JavacWrap(cmd + " " + path);
        wrapper.start();
        int exitValue = wrapper.result();
        if (exitValue == 0)
        {
            return true;
        }

        if (exitValue == -13)
        {
            System.out.println("Forced termination while compiling " + path);
            return false;
        }
        System.out.println("Error while compiling " + path);
        BufferedReader br = new BufferedReader(new InputStreamReader(wrapper.getErrorStream()));
        String line;
        while ((line = br.readLine()) != null)
        {
            System.out.println(line);
        }
        br = new BufferedReader(new InputStreamReader(wrapper.getInputStream()));
        while ((line = br.readLine()) != null)
        {
            System.out.println(line);
        }

        return false;
    }

    private boolean compare(final String clname)
    {
        File originalDir = new File(buildDir);
        File[] origFiles = originalDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(clname) && name.endsWith(".class");
            }
        });

        // Calculating destination directory
        String packageDir = originalDir.getAbsolutePath();
        packageDir = packageDir.substring(testDir.length());

        boolean res = true;
        for (int i = 0; i < origFiles.length && res; i++)
        {
            String testFile = buildDir + packageDir + fs + origFiles[i].getName();
            String orgFile = recompileDir + packageDir + fs + origFiles[i].getName();
            res &= FileComparator.compare(orgFile, testFile);
        }

        return res;
    }

    public FileFilter getInputFileFilter()
    {
        return this;
    }

    public boolean accept(File pathname)
    {
        if (isPrecompile)
        {
            return pathname.isDirectory() || pathname.getName().endsWith(".java");
        }

        String name = pathname.getName();
        boolean contains$ = name.indexOf('$') != -1;
        int ind = name.lastIndexOf('.');
        if (ind != -1)
        {
            if ("class".equals(name.substring(ind + 1)) && !contains$)
            {
                return true;
            }
        }
        return pathname.isDirectory() && !RECOMPILE_DIR.equals(pathname.getName());
    }
}
