package com.sa.mbg.beetl;

import com.google.common.collect.Lists;
import com.sa.util.FileHelper;
import com.sa.util.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.messages.Messages;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class BeetlTemplatesPlugin extends PluginAdapter {
    private String targetDir;
    private String templateRootDir;
    private boolean firstTime =true;
    private ShellCallback shellCallback = null;
    private boolean overwrite = false;
    private GroupTemplate groupTemplate;

    private static final String replaceFromSeparator;

    private static final String replaceToSeparator;

    static {

        if(System.getProperty("os.name").toLowerCase().startsWith("win")){
            replaceFromSeparator = "/";
            replaceToSeparator = "\\\\";
        }else{
            replaceFromSeparator = "\\\\";
            replaceToSeparator = "/";
        }

    }
    public BeetlTemplatesPlugin() {}

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String overwriteStr = this.properties.getProperty("overwrite");
        overwrite = StringUtils.isBlank(overwriteStr)?false:Boolean.parseBoolean(overwriteStr);
        shellCallback = new DefaultShellCallback(overwrite);
    }

    @Override
    public boolean validate(List<String> warnings) {
        boolean valid = true;
        if (!StringUtility.stringHasValue(this.properties.getProperty("targetDir"))) {
            warnings.add(Messages.getString("ValidationError.18", "MapperConfigPlugin", "targetDir"));
            valid = false;
        }
        if (!StringUtility.stringHasValue(this.properties.getProperty("templateRootDir"))) {
            warnings.add(Messages.getString("ValidationError.18", "MapperConfigPlugin", "templateRootDir"));
            valid = false;
        }
        targetDir = this.properties.getProperty("targetDir");
        templateRootDir = this.properties.getProperty("templateRootDir");
        return valid;
    }


    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {

        generateFile(introspectedTable);
        ArrayList answer1 = new ArrayList(0);
        return answer1;
    }


    private void generateFile(IntrospectedTable introspectedTable) {
        try {
            List<File> files = unzipIfTemplateRootDirIsZipFile(Lists.newArrayList(new File(templateRootDir)));
            List<File> childFiles = Lists.newArrayList();
            childFiles = ergodic(files.get(0),childFiles);

            if(childFiles == null || childFiles.isEmpty()){
                return;
            }
            for(File childFile : childFiles){


                if(childFile.getCanonicalPath().contains("${classNameFirstLower}") || childFile.getCanonicalPath().contains("${className}") || firstTime){
                    generateFile(childFile, introspectedTable);
                }
            }
            firstTime=false;
        } catch (ShellException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void generateFile(File childFile, IntrospectedTable introspectedTable) throws IOException, ShellException {
        if(childFile.isDirectory()){
            File dir = new File(getDirRelativePath(childFile.getCanonicalPath(), introspectedTable));
            if(!dir.exists()) {
                dir.mkdirs();
            }
            return;
        }

        if(childFile.getCanonicalPath().lastIndexOf(".btl") == -1){
            File targetFile = new File(targetDir, getFileRelativePath(childFile.getCanonicalPath(), introspectedTable));

            if(targetFile.exists() && !overwrite){
                return;
            }
            FileUtils.copyFile(childFile, targetFile);
            return;
        }
        String className = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        String classNameFirstLower = StringUtils.uncapitalize(className);
        Template bodyVM = getFileBeetlGroupTemplate().getTemplate(getFileRelativePath(childFile.getCanonicalPath()));

        if(bodyVM == null){
            bodyVM = getClasspathBeetlGroupTemplate().getTemplate(getFileRelativePath(childFile.getCanonicalPath()));
        }
        StringWriter screenContent = new StringWriter();
        bodyVM.binding(properties);
        bodyVM.binding(TemplateConstants.className, className);
        bodyVM.binding(TemplateConstants.classNameFirstLower, classNameFirstLower);
        bodyVM.binding(TemplateConstants.table, introspectedTable);
        bodyVM.renderTo(screenContent);
        write(getFileRelativePath(childFile.getCanonicalPath(), introspectedTable), screenContent.toString());
    }


    private String getDirRelativePath(String fileName, IntrospectedTable introspectedTable){
        String className = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        String classNameFirstLower = StringUtils.uncapitalize(className);
        String finalName = targetDir.replaceAll(replaceFromSeparator, replaceToSeparator)+fileName.substring(fileName.indexOf(templateRootDir.replaceAll(replaceFromSeparator, replaceToSeparator))+templateRootDir.length());
        finalName = finalName.replaceAll("\\$\\{classNameFirstLower\\}", classNameFirstLower).replaceAll("\\$\\{className\\}", className);
        return replaceAllProperties(finalName);
    }


    private String getFileRelativePath(String fileName, IntrospectedTable introspectedTable){
        String className = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        String classNameFirstLower = StringUtils.uncapitalize(className);
        if(fileName.lastIndexOf(".btl") == -1){
            String finalName = fileName.substring(fileName.indexOf(templateRootDir.replaceAll(replaceFromSeparator, replaceToSeparator))+templateRootDir.length());
            finalName = finalName.replaceAll("\\$\\{classNameFirstLower\\}", classNameFirstLower).replaceAll("\\$\\{className\\}", className);
            return replaceAllProperties(finalName);
        }
        String finalName = fileName.substring(fileName.indexOf(templateRootDir.replaceAll(replaceFromSeparator, replaceToSeparator))+templateRootDir.length(), fileName.lastIndexOf(".btl"));
        finalName = finalName.replaceAll("\\$\\{classNameFirstLower\\}", classNameFirstLower).replaceAll("\\$\\{className\\}", className);
        return replaceAllProperties(finalName);
    }


    private String replaceAllProperties(String fileName){
        for(String key : this.properties.stringPropertyNames()){
            fileName = fileName.replaceAll("\\$\\{"+key+"\\}",properties.getProperty(key));
        }
        return fileName;
    }


    @org.jetbrains.annotations.NotNull
    private String getFileRelativePath(String fileName){
        return fileName.substring(fileName.indexOf(templateRootDir.replaceAll(replaceFromSeparator, replaceToSeparator))+templateRootDir.length());
    }


    private void write(String relativeFileName, String content) throws ShellException, IOException {


        File targetFile = new File(targetDir, relativeFileName);

        if(targetFile.exists() && !overwrite){
            return;
        }
        if(!targetFile.exists()) {
            File parent = new File(targetFile.getParent());
            if(!parent.exists()) {
                new File(targetFile.getParent()).mkdirs();
            }
            targetFile.createNewFile();
        }
        this.writeFile(targetFile, content, "UTF-8");
    }


    private void writeFile(File file, String content, String fileEncoding) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        OutputStreamWriter osw;
        if (fileEncoding == null) {
            osw = new OutputStreamWriter(fos);
        } else {
            osw = new OutputStreamWriter(fos, fileEncoding);
        }
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(content);
        bw.close();
    }


    private List<File> ergodic(File file,List<File> resultFile){
        File[] files = file.listFiles();
        if(files==null) {
            return resultFile;
        }
        for (File f : files) {
            if(f.isDirectory()){
                resultFile.add(f);
                ergodic(f,resultFile);
            }else {
                resultFile.add(f);
            }
        }
        return resultFile;
    }


    private List<File> unzipIfTemplateRootDirIsZipFile(ArrayList<File>     templateRootDirs) throws MalformedURLException {
        List<File> unzipIfTemplateRootDirIsZipFile = new ArrayList<File>();
        for (int i = 0; i < templateRootDirs.size(); i++) {
            File file = templateRootDirs.get(i);
            String templateRootDir = FileHelper.toFilePathIfIsURL(file);

            String subFolder = "";
            int zipFileSeperatorIndexOf = templateRootDir.indexOf("!");
            if (zipFileSeperatorIndexOf >= 0) {
                subFolder = templateRootDir.substring(zipFileSeperatorIndexOf + 1);
                templateRootDir = templateRootDir.substring(0, zipFileSeperatorIndexOf);
            }

            if (new File(templateRootDir).isFile()) {
                File tempDir = ZipUtils.unzip2TempDir(new File(templateRootDir),
                        "tmp_generator_template_folder_for_zipfile");
                unzipIfTemplateRootDirIsZipFile.add(new File(tempDir, subFolder));
            } else {
                unzipIfTemplateRootDirIsZipFile.add(new File(templateRootDir, subFolder));
            }
        }
        return unzipIfTemplateRootDirIsZipFile;
    }


    private GroupTemplate getFileBeetlGroupTemplate() throws IOException {
        if(groupTemplate == null) {

            FileResourceLoader resourceLoader = new FileResourceLoader(templateRootDir,"utf-8");
            Configuration cfg = Configuration.defaultConfiguration();
            cfg.add("/mbg-templates/btl/beetl.properties");
            groupTemplate = new GroupTemplate(resourceLoader, cfg);
            return groupTemplate;
        }
        return groupTemplate;
    }


    private GroupTemplate getClasspathBeetlGroupTemplate() throws IOException {
        if(groupTemplate == null) {
            ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(templateRootDir, "utf-8");

            Configuration cfg = Configuration.defaultConfiguration();
            cfg.add("/mbg-templates/btl/beetl.properties");
            groupTemplate = new GroupTemplate(resourceLoader, cfg);
            return groupTemplate;
        }
        return groupTemplate;
    }


}
