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
    public String getFeatures(@RequestParam(value = "inputFile") MultipartFile[] inputFiles,
                                    @RequestParam(value = "ontFile") MultipartFile[] ontologyFiles) throws Exception {

        File inputFile = null;
        for (MultipartFile file : inputFiles) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("txt")) {
                    inputFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(inputFile);
                    } catch (IOException e) {
                        return e.getMessage();
                    }
                }
            }
        }

        File ontologyFile = null;
        for (MultipartFile file : ontologyFiles) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("owl")) {
                    ontologyFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(ontologyFile);
                    } catch (IOException e) {
                        return e.getMessage();
                    }
                }
            }
        }

        File inputDirectory = new File("C:\\temp\\noble\\input\\");
        if (inputDirectory.exists())
            FileUtils.forceDelete(inputDirectory);
        inputDirectory.mkdirs();
        assert inputFile != null;
        FileUtils.copyFileToDirectory(inputFile,inputDirectory);

        File ontDirectory = new File("C:\\temp\\noble\\ont\\");
        if (ontDirectory.exists())
            FileUtils.forceDelete(ontDirectory);
        ontDirectory.mkdirs();
        assert ontologyFile != null;
        FileUtils.copyFileToDirectory(ontologyFile,ontDirectory);


        File output = new File("C:\\temp\\noble\\output\\");
        if (output.exists()){
            FileUtils.forceDelete(output);
        }
        output.mkdirs();


        Map<String, String> pathMap = new HashMap<>();
        pathMap.put("ont", ontDirectory.getAbsolutePath());
        pathMap.put("input", inputDirectory.getAbsolutePath());
        pathMap.put("output", output.getAbsolutePath());

        LOGGER.debug("\nSending request to Noblementions\n");

        noblementionsConnector.processNobleMentions(pathMap);

        LOGGER.debug("\nReading contents from Noblementions\n");
        String contents = FileUtils.readFileToString(new File(output + "/RESULTS.tsv"));

        return Converters.tsvToCsv(contents);
    }
}
