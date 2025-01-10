package org.wso2.carbon.esb.module.ai.operations;

import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.esb.module.ai.AbstractAIMediator;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class DocParser extends AbstractAIMediator {

    // Text content types
    private static final String TEXT_TYPE = "text/plain";
    private static final String JSON_TYPE = "application/json";
    private static final String XML_TYPE = "application/xml";
    private static final String HTML_TYPE = "text/html";
    private static final String MARKDOWN_TYPE = "text/markdown";

    // PDF content type
    private static final String PDF_TYPE = "application/pdf";

    // Document content types
    private static final String DOC_TYPE = "application/msword";
    private static final String DOCX_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String PPT_TYPE = "application/vnd.ms-powerpoint";
    private static final String PPTX_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    private static final String XLS_TYPE = "application/vnd.ms-excel";
    private static final String XLSX_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    enum PARSER {
        TEXT,
        PDF_BOX,
        POI
    }

    @Override
    public void initialize(MessageContext mc) {
    }

    @Override
    public void execute(MessageContext mc) {
        String input = getMediatorParameter(mc, "input", String.class, false);
        String contentType = getMediatorParameter(mc, "contentType", String.class, false);
        String responseVariable = getMediatorParameter(mc, "responseVariable", String.class, false);

        PARSER parser = null;
        parser = contentType.equalsIgnoreCase("auto") ? autoDetectParser(mc) : determineParser(contentType);
        input = input.equalsIgnoreCase("payload") ? mc.getEnvelope().getBody().getFirstElement().getText() : input;

        DocumentParser docParser = null;
        ByteArrayInputStream inputStream = null;
        switch (parser) {
            case TEXT:
                docParser = new TextDocumentParser();
                inputStream = new ByteArrayInputStream(input.getBytes());
                break;
            case PDF_BOX:
                docParser = new ApachePdfBoxDocumentParser();
                inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(input));
                break;
            case POI:
                docParser = new ApachePoiDocumentParser();
                inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(input));
                break;
            default:
                handleException("Unsupported content type: " + contentType, mc);
        }

        String document = docParser.parse(inputStream).text();
        mc.setProperty(responseVariable, document);
    }

    private PARSER determineParser(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "text", "json", "xml", "html", "markdown" -> PARSER.TEXT;
            case "pdf" -> PARSER.PDF_BOX;
            case "doc", "docx", "ppt", "pptx", "xls", "xlsx" -> PARSER.POI;
            default -> null;
        };
    }

    private PARSER autoDetectParser(MessageContext mc) {
        Object payloadType = ((Axis2MessageContext) mc).getAxis2MessageContext().getProperty("ContentType");
        if (payloadType == null) {
            handleException("Content type cannot be determined", mc);
        }
        return switch (payloadType.toString().toLowerCase()) {
            case TEXT_TYPE, JSON_TYPE, XML_TYPE, HTML_TYPE, MARKDOWN_TYPE -> PARSER.TEXT;
            case PDF_TYPE -> PARSER.PDF_BOX;
            case DOC_TYPE, DOCX_TYPE, PPT_TYPE, PPTX_TYPE, XLS_TYPE, XLSX_TYPE -> PARSER.POI;
            default -> null;
        };
    }
}
