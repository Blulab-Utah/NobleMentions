package edu.utah.blulab.controller;

import edu.utah.blulab.constants.ServiceConstants;
import edu.utah.blulab.services.INoblementionsConnector;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.logging.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.mail.Multipart;
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
    public ModelAndView getFeatures(@RequestParam("Input") String input,
                                    @RequestParam("Output") String output,
                                    @RequestParam("file") MultipartFile[] files) throws Exception {


        File ontologyFile = null;
        String ontologyContent = null;
        Map<String, String> pathMap = new HashMap<>();
        pathMap.put("input", input);
        pathMap.put("output", output);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("owl")) {
                    ontologyFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(ontologyFile);
                    } catch (IOException e) {
                        return createErrorResponse(e.getMessage());
                    }
                }
            }
        }
        if (null != ontologyFile) {
            ontologyContent = FileUtils.readFileToString(ontologyFile, "UTF-8");
        } else {
            return createErrorResponse("Empty Ontology File Found");
        }


        LOGGER.debug("\nSending request to Noblementions\n");
        noblementionsConnector.processNobleMentions(pathMap, ontologyContent);

//        noblementionsConnector.processNobleMentions(pathMap);

        LOGGER.debug("\nReading contents from Noblementions\n");
        String contents = FileUtils.readFileToString(new File(output + "/RESULTS.tsv"));

        String contentToJson = Converters.tsvToJson(contents);

        return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, contentToJson);
    }

    private ModelAndView createErrorResponse(String sMessage) {
        return new ModelAndView(jsonView, ServiceConstants.ERROR_FIELD, sMessage);
    }
}
