import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "json-concatenator", defaultPhase = LifecyclePhase.INSTALL)
public class JsonResourceConcatenatorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject mavenProject;

    @Parameter(property = "globalJsonFilename")
    String globalJsonFilename;

    private List<CustomConfiguration> customConfigurationsFinalList = new ArrayList<>();


    public void execute() throws MojoExecutionException, MojoFailureException {
        File file = new File(globalJsonFilename);
        FileWriter fileWriter = null;
        try {
            file.createNewFile();
            fileWriter = new FileWriter(file);

        } catch (IOException x) {
            System.out.println("File Creation error");
        }

        List<Resource> dependencies = mavenProject.getResources();

        Gson gson = new Gson();
        try {
            FileWriter finalFileWriter1 = fileWriter;
            finalFileWriter1.write("{\n" +
                    "  \"customConfigurations\": [");
            dependencies.stream().forEach(e -> {

                try (
                        Stream<Path> paths = Files.walk(Paths.get(e.getDirectory()))) {
                    paths
                            .filter(c -> c.toString().endsWith(".json"))
                            .forEach(d -> {
                                try {
                                    FileInputStream fis = new FileInputStream(String.valueOf(d.toString()));
                                    String data = IOUtils.toString(fis, "UTF-8");

                                    CustomConfigurationList customConfigurations = gson.fromJson(data, CustomConfigurationList.class);
                                    Iterator<CustomConfiguration> iterator = customConfigurations.getCustomConfigurations().iterator();
                                    while(iterator.hasNext()){
                                        String configListinString = gson.toJson(iterator.next());
                                        finalFileWriter1.write(configListinString);

                                            finalFileWriter1.write(",");


                                    }

//
//                                    for(CustomConfiguration customConfiguration: customConfigurations.getCustomConfigurations()){
//
//                                        String configListinString = gson.toJson(customConfiguration);
//
//                                        finalFileWriter1.write(configListinString);
//                                        finalFileWriter1.write(",");
//
//                                    }


                                } catch (Exception x) {
                                    System.out.println(x);
                                }
                            });


                } catch (IOException io) {
                    System.out.println("Path not Found");
                }

            });
            finalFileWriter1.write("{\n" +
                    "\t\t\"configId\": \"DEFAULT_ID\",\n" +
                    "\t\t\"configValue\": \"DEFAULT_VALUE\"\n" +
                    "\t}");
            finalFileWriter1.write("]\n" +
                    "}");
            finalFileWriter1.flush();
            finalFileWriter1.close();
        } catch (IOException e) {
            System.out.println("error here");
        }
    }
}
