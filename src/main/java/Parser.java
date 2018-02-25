import domain.UserTask;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static final String TYPICAL_ENTER_SCRIPT = "java.util.logging.Logger log = java.util.logging.Logger.getLogger(kcontext.getProcessInstance().getProcessName());\n" +
            "Ref019 _ref_19 = (Ref019) ref_19List.get(%s.getRefId());\n" + //aItem_XX
            "kcontext.setVariable(\"dueDateForTask\", java.time.OffsetDateTime.now().plusHours((long) (_ref_19.getWorkDays() * 24)).format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd'T'HH:mm:ssZZ\")));\n" +
            "java.util.Date _today = new java.util.Date();\n" +
            "%s.setExactStartDate(_today);%s.setIsCompleted(false);\n" + //aItem_XX //aItem_XX
            "businessTaskDataMap.put(\"taskName\", \"primaryPlanTask\");\n" +
            "ObjectMapper mapper = new ObjectMapper();\n" +
            "String jsonDataString = mapper.writeValueAsString(businessTaskDataMap);\n" +
            "kcontext.setVariable(\"businessTaskData\", jsonDataString);\n" +
            "log.info(\"%s \" + %s);\n";//aItem_XX

    private static final String TYPICAL_EXIT_SCRIPT = "java.util.logging.Logger log = java.util.logging.Logger.getLogger(kcontext.getProcessInstance().getProcessName());\n" +
            "java.util.Date _today = new java.util.Date();\n" +
            "%s.setExactCompleteDate(_today);\n" + //aItem_XX
            "%s.setIsCompleted(true);\n" + //aItem_XX
            "log.info(\"User task %s is finished: \" + %s);\n"; //aItem_XX //aItem_XX

    private static final String TYPICAL_INIT_SCRIPT = "java.util.logging.Logger log = java.util.logging.Logger.getLogger(kcontext.getProcessInstance().getProcessName());\n" +
            "log.info(String.format(\"ReorganizationProcess start, process %s\", kcontext.getProcessInstance().getId()));\n" +
            "\n" +
            "Map<String, String> _businessTaskDataMap = new HashMap<String, String>();\n" +
            "_businessTaskDataMap.put(\"clientFullName\", clientFullName);\n" +
            "kcontext.setVariable(\"businessTaskDataMap\", _businessTaskDataMap);\n" +
            "\n" +
            "/* Init stream List variables  end */\n" +
            "Map<Integer, ActionPlanItem> _actionPlanItemsMapInit = new HashMap<Integer, ActionPlanItem>();\n" +
            "\n" +
            "List<ActionPlanItem> _actionPlan = (ArrayList) reorganizationPlan;\n" +
            "\n" +
            "for (java.util.Iterator<ActionPlanItem> it = _actionPlan.iterator(); it.hasNext();) {\n" +
            "\tActionPlanItem _actionPlanItem = (ActionPlanItem) it.next();\n" +
            "\t_actionPlanItemsMapInit.put(_actionPlanItem.getRefId(), _actionPlanItem);\n" +
            "}\n" +
            "\n" +
            "kcontext.setVariable(\"_actionPlanItemsMap\", _actionPlanItemsMapInit);\n" +
            "\n" +
            "%s" +
//            "aItem_25 = _actionPlanItemsMapInit.containsKey(25) ? (ActionPlanItem) _actionPlanItemsMapInit.get(25) : null;\n" +
//            "aItem_26 = _actionPlanItemsMapInit.containsKey(26) ? (ActionPlanItem) _actionPlanItemsMapInit.get(26) : null;\n" +
//            "aItem_27 = _actionPlanItemsMapInit.containsKey(27) ? (ActionPlanItem) _actionPlanItemsMapInit.get(27) : null;\n" +
//            "aItem_28 = _actionPlanItemsMapInit.containsKey(28) ? (ActionPlanItem) _actionPlanItemsMapInit.get(28) : null;\n" +
            "\n" +
            "%s" +
//            "kcontext.setVariable(\"aItem_25\", aItem_25);\n" +
//            "kcontext.setVariable(\"aItem_26\", aItem_26);\n" +
//            "kcontext.setVariable(\"aItem_27\", aItem_27);\n" +
//            "kcontext.setVariable(\"aItem_28\", aItem_28);\n" +
            "\n" +
            "ref_19List = (ArrayList) references.get(\"ref_19\");\n" +
            "kcontext.setVariable(\"ref_19List\", ref_19List);";

    public void parse(String fileName) {
        try {

            StringBuilder initStringPartOne = new StringBuilder();
            StringBuilder initStringPartTwo = new StringBuilder();
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(fileName);

            List<UserTask> userTaskList = new ArrayList<>();

            NodeList tasks = document.getElementsByTagName("bpmn2:userTask");
            for (int i = 0; i < tasks.getLength(); i++) {
                Element userTask = (Element) tasks.item(i);
                String id = userTask.getAttribute("id");

                Element extensionElements = (Element) userTask.getElementsByTagName("bpmn2:extensionElements").item(0);

                Element metaData = (Element) extensionElements.getElementsByTagName("drools:metaData").item(0);
                Element metaValue = (Element) metaData.getElementsByTagName("drools:metaValue").item(0);
                String elementName = metaValue.getTextContent();

                Element onEntryScriptNode = (Element) extensionElements.getElementsByTagName("drools:onEntry-script").item(0);
                String entryScript = onEntryScriptNode.getElementsByTagName("drools:script").item(0).getTextContent();

                Element onExitScriptNode = (Element) extensionElements.getElementsByTagName("drools:onExit-script").item(0);
                String exitScript = onExitScriptNode.getElementsByTagName("drools:script").item(0).getTextContent();

                UserTask userTaskInstance = new UserTask(id, elementName, entryScript, exitScript);
                userTaskList.add(userTaskInstance);

            }
//fill
//            for (UserTask userTask : userTaskList) {

            NodeList associations = document.getElementsByTagName("bpmn2:association");
            for (int i = 0; i < associations.getLength(); i++) {
                Node item = associations.item(i);
                String sourceRef = item.getAttributes().getNamedItem("sourceRef").getNodeValue();
                String targetRef = item.getAttributes().getNamedItem("targetRef").getNodeValue();
                for (UserTask userTask : userTaskList) {
                    if (userTask.getId().equals(sourceRef)) {
                        userTask.setStepRefId(targetRef);
                        break;
                    }
                }
            }

            NodeList annotationList = document.getElementsByTagName("bpmn2:textAnnotation");
            for (UserTask userTask : userTaskList) {
                String stepRefId = userTask.getStepRefId();

                for (int i = 0; i < annotationList.getLength(); i++) {
                    Element item = (Element) annotationList.item(i);
                    String id = item.getAttributes().getNamedItem("id").getNodeValue();
                    if (stepRefId.equals(id)) {
                        Element extensionElements = (Element) item.getElementsByTagName("bpmn2:extensionElements").item(0);

                        Element metaData = (Element) extensionElements.getElementsByTagName("drools:metaData").item(0);
                        Element metaValue = (Element) metaData.getElementsByTagName("drools:metaValue").item(0);
                        String stepName = metaValue.getTextContent();

                        String[] split = stepName.split("[\\(\\)]");

                        userTask.setLocalStepName(split[0]);
                        userTask.setGeneralStepName(split[1]);
                    }
                }
            }
            for (UserTask userTask : userTaskList) {

                String stepName = userTask.getGeneralStepName();
                String elementName = "aItem_" + stepName;

                initStringPartOne.append(elementName).append(" = _actionPlanItemsMapInit.containsKey(").append(stepName).append(") ? (ActionPlanItem) _actionPlanItemsMapInit.get(").append(stepName).append(") : null;\n");
                initStringPartTwo.append("kcontext.setVariable(\"").append(elementName).append("\"").append(", ").append(elementName).append(");\n");
                //kcontext.setVariable(\"aItem_25\", aItem_25);\n
                Element rootElement = document.getDocumentElement();
                Element newElement = document.createElement("bpmn2:itemDefinition");
                newElement.setAttribute("id", "_" + elementName + "Item");
                newElement.setAttribute("structureRef", "org.rshb.collection.ActionPlanItem");
                rootElement.insertBefore(newElement, rootElement.getFirstChild());
//    <bpmn2:property id="aItem_28" itemSubjectRef="_aItem_28Item"/>
                Element process = (Element) rootElement.getElementsByTagName("bpmn2:process").item(0);
                Element processProperty = document.createElement("bpmn2:property");
                processProperty.setAttribute("id", elementName);
                processProperty.setAttribute("itemSubjectRef", "_" + elementName + "Item");
                process.insertBefore(processProperty, process.getFirstChild().getNextSibling());

                String enterScript = String.format(TYPICAL_ENTER_SCRIPT, elementName, elementName, elementName, elementName, elementName);
                userTask.setEnterScript(enterScript);

                String exitScript = String.format(TYPICAL_EXIT_SCRIPT, elementName, elementName, elementName, elementName);
                userTask.setExitScript(exitScript);

//                System.out.println(exitScript);
            }

            for (int i = 0; i < tasks.getLength(); i++) {
                Element userTask = (Element) tasks.item(i);
                String currentId = userTask.getAttribute("id");


                for (UserTask task : userTaskList) {
                    String id = task.getId();
                    if (id.equals(currentId)) {
                        Element extensionElements = (Element) userTask.getElementsByTagName("bpmn2:extensionElements").item(0);
                        Element onEntryScriptNodeParent = (Element) extensionElements.getElementsByTagName("drools:onEntry-script").item(0);
                        CDATASection enterCDATA = document.createCDATASection(task.getEnterScript());
                        Node enterScriptNode = onEntryScriptNodeParent.getElementsByTagName("drools:script").item(0);
                        if (enterScriptNode.getFirstChild() != null) {
                            enterScriptNode.removeChild(enterScriptNode.getFirstChild());
                        }
                        enterScriptNode.appendChild(enterCDATA);

                        Element onExitScriptNodeParent = (Element) extensionElements.getElementsByTagName("drools:onExit-script").item(0);
                        Node exitScriptNode = onExitScriptNodeParent.getElementsByTagName("drools:script").item(0);
                        CDATASection exitCDATA = document.createCDATASection(task.getExitScript());
                        if (exitScriptNode.getFirstChild() != null) {
                            exitScriptNode.removeChild(exitScriptNode.getFirstChild());
                        }
                        exitScriptNode.appendChild(exitCDATA);

                        boolean inputExist = false;//actionPlanItemInput
                        NodeList inputs = userTask.getElementsByTagName("bpmn2:dataInputAssociation");
                        for (int y = 0; y < inputs.getLength(); y++) {
                            Element item = (Element) inputs.item(y);
                            Node targetRef = item.getElementsByTagName("bpmn2:targetRef").item(0);
                            if (targetRef.getTextContent().contains("actionPlanItemInput")) {
                                inputExist = true;
                                item.getElementsByTagName("bpmn2:sourceRef").item(0).setTextContent("aItem_" + task.getGeneralStepName());
                                break;
                            }
                        }

                        if (!inputExist) {
                            throw new RuntimeException("Input file not contain input associations");
                        }

                        boolean outputExist = false;

                        NodeList outputs = userTask.getElementsByTagName("bpmn2:dataOutputAssociation");
                        for (int y = 0; y < outputs.getLength(); y++) {
                            Element item = (Element) outputs.item(y);
                            Node targetRef = item.getElementsByTagName("bpmn2:sourceRef").item(0);
                            if (targetRef.getTextContent().contains("actionPlanItemOutput")) {
                                outputExist = true;
                                item.getElementsByTagName("bpmn2:targetRef").item(0).setTextContent("aItem_" + task.getGeneralStepName());
                                break;
                            }
                        }

                        if (!outputExist) {
                            throw new RuntimeException("Input file not contain output associations");
                        }


                    }
                }
            }


//            Writer writer = new FileWriter("MyOutput.xml");
//            XMLSerializer xml = new XMLSerializer(writer, null);
//            xml.serialize(document);
            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(new File("MyOutput.xml"));
            StreamResult result = new StreamResult(writer);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

//                System.out.println(userTask);
//            }

//            for (int i = 0; i < nodes.getLength(); i++) {
//                Node node = nodes.item(i);
//                // Если нода не текст, то это книга - заходим внутрь
//                System.out.println(node.getNodeName());
//                if ("bpmn2:userTask".equals(node.getNodeName())) {
//                    node.getAttributes().getNamedItem("id");
//
//                    NodeList childNodes = node.getChildNodes();
//                    for (int y = 0; y < childNodes.getLength(); y++) {
//                        Node childNode = childNodes.item(y);
//                        System.out.println(childNode.getNodeName());
//                    }
//                }

//                if (node.getAttributes() != null) {
//                    System.out.println(node.getAttributes().getLength());
//                }
//                System.out.println(node);
//                    NodeList bookProps = node.getChildNodes();
//                    for(int j = 0; j < bookProps.getLength(); j++) {
//                        Node bookProp = bookProps.item(j);
//                        // Если нода не текст, то это один из параметров книги - печатаем
//                        if (bookProp.getNodeType() != Node.TEXT_NODE) {
//                            System.out.println(bookProp.getNodeName() + ":" + bookProp.getChildNodes().item(0).getTextContent());
//                        }
//                    }
//                    System.out.println("===========>>>>");


        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }
}
