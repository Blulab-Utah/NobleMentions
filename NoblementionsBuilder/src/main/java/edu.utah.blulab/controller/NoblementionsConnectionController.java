package edu.utah.blulab.controller;

import edu.utah.blulab.constants.ServiceConstants;
import edu.utah.blulab.services.INoblementionsConnector;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Controller
public class NoblementionsConnectionController {

    private static final Logger LOGGER = Logger.getLogger(NoblementionsConnectionController.class);

    @Autowired
    private INoblementionsConnector noblementionsConnector;
    @Autowired
    private View jsonView;

    @RequestMapping(value = "/getAnnotations", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelAndView getFeatures(@RequestHeader(value = "Input") String input,
                              @RequestHeader(value = "Output") String output,
                              @RequestHeader(value = "OntologyPath") String ontPath) throws Exception {


        Map<String, String> pathMap = new HashMap<>();
        pathMap.put("ont", ontPath);
        pathMap.put("input", input);
        pathMap.put("output", output);

        LOGGER.debug("\nSending request to Noblementions\n");

        noblementionsConnector.processNobleMentions(pathMap);

        LOGGER.debug("\nReading contents from Noblementions\n");
        String contents = FileUtils.readFileToString(new File(output + "/RESULTS.tsv"));

        String contentToJson = Converters.tsvToJson(contents);

        return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, contentToJson);
    }
}
