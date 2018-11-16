package edu.utah.blulab.controller;

import edu.utah.blulab.constants.ServiceConstants;
import edu.utah.blulab.services.INoblementionsConnector;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
    public String getFeatures(@RequestParam(value = "ontFile") MultipartFile[] ontologyFiles,
                              @RequestParam(value = "ip") MultipartFile[] inputFiles) throws Exception {

        File inputFile = null;
//        File inputDirectory = new File("\\home\\deep\\temp\\noble\\input\\");
        File inputDirectory = new File("C:\\Users\\Deep\\temp\\input\\");
        if (inputDirectory.exists())
            FileUtils.cleanDirectory(inputDirectory);
        else
            inputDirectory.mkdirs();
        for (MultipartFile file : inputFiles) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("txt")) {
                    inputFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(inputFile);
                        FileUtils.copyFileToDirectory(inputFile,inputDirectory);

                    } catch (IOException e) {
                        return e.getMessage();
                    }
                }
            }
        }


        //File ontDirectory = new File("\\home\\deep\\temp\\noble\\ont\\");
        File ontDirectory = new File("C:\\Users\\Deep\\temp\\ontologies\\");
        if (ontDirectory.exists())
            FileUtils.cleanDirectory(ontDirectory);
        else
            ontDirectory.mkdirs();
        File ontologyFile = null;
        for (MultipartFile file : ontologyFiles) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("owl")) {
                    ontologyFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(ontologyFile);
                        FileUtils.copyFileToDirectory(ontologyFile,ontDirectory);
                    } catch (IOException e) {
                        return e.getMessage();
                    }
                }
            }
        }

//        File output = new File("\\home\\deep\\temp\\noble\\output\\");
        File outputDirectory = new File("C:\\Users\\Deep\\temp\\output\\");
        if (outputDirectory.exists()){
            FileUtils.cleanDirectory(outputDirectory);
        }
        else
            outputDirectory.mkdirs();

        Map<String, String> pathMap = new HashMap<>();
        pathMap.put("ont", ontDirectory.getAbsolutePath());
        pathMap.put("input", inputDirectory.getAbsolutePath());
        pathMap.put("output", outputDirectory.getAbsolutePath());

        LOGGER.debug("\nSending request to Noblementions\n");

        try{
            noblementionsConnector.processNobleMentions(pathMap);
        }
        catch (Exception e)
        {
            return e.getMessage();
        }

        LOGGER.debug("\nReading contents from Noblementions\n");
        String contents = FileUtils.readFileToString(new File(outputDirectory + "/RESULTS.tsv"));

//        FileUtils.forceDelete(inputDirectory);
//        FileUtils.forceDelete(outputDirectory);
//        FileUtils.forceDelete(ontDirectory);
        return Converters.tsvToCsv(contents);
    }
}
