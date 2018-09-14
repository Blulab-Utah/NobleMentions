package edu.utah.blulab.controller;

import edu.utah.blulab.services.INoblementionsConnector;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class NoblementionsConnectionController {

    private static final Logger LOGGER = Logger.getLogger(NoblementionsConnectionController.class);

    @Autowired
    private INoblementionsConnector noblementionsConnector;
    @Autowired
    private View jsonView;

    @RequestMapping(value = "/getAnnotations", method = RequestMethod.POST)
    @ResponseBody
    public List<String> getFeatures(@RequestParam(value = "input") MultipartFile[] inputFiles,
                                    @RequestParam(value = "ontFile") MultipartFile[] ontologyFiles) throws Exception {



        File ontologyFile = null;
        for (MultipartFile file : ontologyFiles) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("owl")) {
                    ontologyFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(ontologyFile);
                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }

        File ontDirectory = new File("\\home\\deep\\temp\\noble\\ont\\");
        if (ontDirectory.exists())
            FileUtils.forceDelete(ontDirectory);
        ontDirectory.mkdirs();
        assert ontologyFile != null;
        FileUtils.copyFileToDirectory(ontologyFile,ontDirectory);

        File outputDirectory = new File("\\home\\deep\\temp\\noble\\output\\");
        if (outputDirectory.exists()){
            FileUtils.forceDelete(outputDirectory);
        }
        outputDirectory.mkdirs();

        File inputDirectory = new File("\\home\\deep\\temp\\noble\\input\\");
        if (inputDirectory.exists())
            FileUtils.forceDelete(inputDirectory);
        inputDirectory.mkdirs();

        Map<String, String> pathMap = new HashMap<>();
        pathMap.put("ont", ontDirectory.getAbsolutePath());
        pathMap.put("output", outputDirectory.getAbsolutePath());

        List<String> contentList = new ArrayList<>();

        File inputFile = null;
        for (MultipartFile file : inputFiles) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("txt")) {
                    inputFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(inputFile);
                        FileUtils.copyFileToDirectory(inputFile,inputDirectory);

                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }
        pathMap.put("input", inputDirectory.getAbsolutePath());
        LOGGER.debug("\nSending request to Noblementions\n");

        noblementionsConnector.processNobleMentions(pathMap);

        LOGGER.debug("\nReading contents from Noblementions\n");
        String contents = FileUtils.readFileToString(new File(outputDirectory + "/RESULTS.tsv"));
        contentList.add(contents);
        FileUtils.cleanDirectory(inputDirectory);
        FileUtils.cleanDirectory(outputDirectory);
        FileUtils.cleanDirectory(ontDirectory);
        return contentList;
    }
}
