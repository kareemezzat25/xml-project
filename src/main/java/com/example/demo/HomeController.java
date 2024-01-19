package com.example.demo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.*;
import java.io.File;
import java.util.*;


@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
    @PostMapping("/generate-forms")
    public String generateForms(@RequestParam int numStudents, Model model) {
        model.addAttribute("numForms", numStudents);
        return "forms";
    }
    @GetMapping("/view-all")
    public String viewAllStudents(Model model) {
        try {
            List<Student> students = getAllStudentsFromXML();
            model.addAttribute("students", students);
            return "viewAll";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("result", "Error occurred!");
            return "notFound";
        }
    }

    public List<Student> getAllStudentsFromXML() throws Exception {
        List<Student> students = new ArrayList<>();
        File file = new File("data.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        // Normalize the document
        document.getDocumentElement().normalize();

        NodeList studentList = document.getElementsByTagName("Student");

        for (int i = 0; i < studentList.getLength(); i++) {
            Node studentNode = studentList.item(i);

            if (studentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element studentElement = (Element) studentNode;

                Student student = new Student();
                student.setfirstName(getElementValue(studentElement, "FirstName"));
                student.setLastName(getElementValue(studentElement, "LastName"));
                student.setGender(getElementValue(studentElement, "Gender"));
                student.setId(studentElement.getAttribute("ID"));
                student.setGpa(Double.parseDouble(getElementValue(studentElement, "GPA")));
                student.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
                student.setAddress(getElementValue(studentElement, "Address"));

                students.add(student);
            }
        }

        return students;
    }



    @PostMapping("/store-data")
    public String storeData(@ModelAttribute ListStudent listStudent, Model model) {
        List<Student> itemList = listStudent.getItemList();
        try {
            File xmlFile = new File("data.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document document;
            Element root;

            if (xmlFile.exists()) {
                // File exists, parse the existing XML
                document = dBuilder.parse(xmlFile);
                root = document.getDocumentElement();
            } else {
                // File doesn't exist, create a new XML document
                document = dBuilder.newDocument();
                root = document.createElement("University");
                document.appendChild(root);
            }

            for (Student student : itemList) {
                String studentId = student.getId();

                // Check if the ID already exists in the XML
                boolean idExists = false;
                NodeList studentList = document.getElementsByTagName("Student");
                for (int i = 0; i < studentList.getLength(); i++) {
                    Node studentNode = studentList.item(i);

                    if (studentNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element studentElement = (Element) studentNode;
                        String id = studentElement.getAttribute("ID");

                        // If ID exists,
                        if (id.equals(studentId)) {
                            model.addAttribute("error", "Student with ID " + studentId + " already exists.");
                            return "index";
                        }
                    }
                }

                // If ID doesn't exist, create a new record
                if (!idExists) {
                    Element studentRecord = document.createElement("Student");
                    studentRecord.setAttribute("ID", studentId);
                    root.appendChild(studentRecord);

                    Element firstname = document.createElement("FirstName");
                    firstname.appendChild(document.createTextNode(student.getFirstName()));
                    studentRecord.appendChild(firstname);

                    Element lastName = document.createElement("LastName");
                    lastName.appendChild(document.createTextNode(student.getLastName()));
                    studentRecord.appendChild(lastName);

                    Element gender = document.createElement("Gender");
                    gender.appendChild(document.createTextNode(student.getGender()));
                    studentRecord.appendChild(gender);

                    Element gpa = document.createElement("GPA");
                    gpa.appendChild(document.createTextNode(String.valueOf(student.getGpa())));
                    studentRecord.appendChild(gpa);

                    Element level = document.createElement("Level");
                    level.appendChild(document.createTextNode(String.valueOf(student.getLevel())));
                    studentRecord.appendChild(level);

                    Element address = document.createElement("Address");
                    address.appendChild(document.createTextNode(student.getAddress()));
                    studentRecord.appendChild(address);
                }
            }

            // Write the updated XML back to the file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        return "search-delete";
    }
    @GetMapping("/updateStudent/{id}")
    public String showUpdateForm(@PathVariable String id, Model model) {

        try {
            List<Student> students = getAllStudentsFromXML();
            Optional<Student> studentOptional = students.stream().filter(student -> student.getId().equals(id)).findFirst();

            if (studentOptional.isPresent()) {
                model.addAttribute("student", studentOptional.get());
                return "updateStudent";
            } else {
                model.addAttribute("result", "Student not found!");
                return "notFound";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("result", "Error occurred!");
            return "error";
        }
    }
    @PostMapping("/updateStudent/{id}")
    public String updateStudentDetails(@PathVariable String id, @ModelAttribute Student updatedStudent, Model model) {
        try {
            List<Student> students = getAllStudentsFromXML();
            Optional<Student> studentOptional = students.stream().filter(student -> student.getId().equals(id)).findFirst();

            if (studentOptional.isPresent()) {
                Student existingStudent = studentOptional.get();

                // Update the existing student details
                existingStudent.setfirstName(updatedStudent.getFirstName());
                existingStudent.setLastName(updatedStudent.getLastName());
                existingStudent.setGender(updatedStudent.getGender());
                existingStudent.setGpa(updatedStudent.getGpa());
                existingStudent.setLevel(updatedStudent.getLevel());
                existingStudent.setAddress(updatedStudent.getAddress());

                // Update the XML file with the modified data
                updateXmlFile(students);

                model.addAttribute("result", "Student details updated successfully!");
                return "studentUpdated";
            } else {
                model.addAttribute("result", "Student not found!");
                return "notFound";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("result", "Error occurred!");
            return "error";
        }
    }


    // ... (existing code)

    private Element createXmlElementFromStudent(Student student) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.newDocument();

            Element studentRecord = document.createElement("Student");
            studentRecord.setAttribute("ID", student.getId());

            Element firstname = document.createElement("FirstName");
            firstname.appendChild(document.createTextNode(student.getFirstName()));
            studentRecord.appendChild(firstname);

            Element lastName = document.createElement("LastName");
            lastName.appendChild(document.createTextNode(student.getLastName()));
            studentRecord.appendChild(lastName);

            Element gender = document.createElement("Gender");
            gender.appendChild(document.createTextNode(student.getGender()));
            studentRecord.appendChild(gender);

            Element gpa = document.createElement("GPA");
            gpa.appendChild(document.createTextNode(String.valueOf(student.getGpa())));
            studentRecord.appendChild(gpa);

            Element level = document.createElement("Level");
            level.appendChild(document.createTextNode(String.valueOf(student.getLevel())));
            studentRecord.appendChild(level);

            Element address = document.createElement("Address");
            address.appendChild(document.createTextNode(student.getAddress()));
            studentRecord.appendChild(address);

            return studentRecord;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
        return null;
    }

    // ... (existing code)

    @GetMapping("/search-page")
    public String showSearchPage() {
        return "search_page";
    }


    @GetMapping("delete")
    public String showDeletepage()
    {
        return "deleteById";
    }
    @GetMapping("/sort")
    public String sortFile() {
        return "sort";
    }

    @GetMapping("/footer")
    public String footer()
    {
        return "search-delete";
    }


    @PostMapping("/search")
    public String searchStudents(@RequestParam(value = "id", required = false) String id,
                                 @RequestParam(value = "firstName", required = false) String firstName,
                                 @RequestParam(value = "lastName", required = false) String lastName,
                                 @RequestParam(value = "gpa", required = false) String gpa,
                                 @RequestParam(value = "level", required = false) String level,
                                 @RequestParam(value = "address", required = false) String address,
                                 @RequestParam(value = "gender", required = false) String gender,
                                 Model model) {
        try {
            List<Student> resultList = new ArrayList<>();
            File xmlFile = new File("data.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(xmlFile);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // Build XPath expression based on provided search parameters
            String expression = "//Student";

            if (id != null && !id.isEmpty()) {
                expression += "[@ID='" + id + "']";
            }
            if (firstName != null && !firstName.isEmpty()) {
                expression += "[contains(FirstName, '" + firstName + "')]";
            }
            if (lastName != null && !lastName.isEmpty()) {
                expression += "[contains(LastName, '" + lastName + "')]";
            }
            if (gpa != null && !gpa.isEmpty()) {
                expression += "[number(GPA)=" + Double.parseDouble(gpa) + "]";
            }
            if (level != null && !level.isEmpty()) {
                expression += "[contains(Level, '" + level + "')]";
            }
            if (address != null && !address.isEmpty()) {
                expression += "[contains(Address, '" + address + "')]";
            }
            if (gender != null && !gender.isEmpty()) {
                expression += "[contains(Gender, '" + gender + "')]";
            }

            NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element studentElement = (Element) nodeList.item(i);
                Student student = createStudentFromXmlElement(studentElement);
                resultList.add(student);
            }

            model.addAttribute("resultList", resultList);
            model.addAttribute("numFound", resultList.size());
            return "result";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("result", "Error occurred!");
            return "error";
        }
    }
    private Student createStudentFromXmlElement(Element studentElement) {
        Student student = new Student();
        student.setId(studentElement.getAttribute("ID"));
        student.setfirstName(getElementValue(studentElement, "FirstName"));
        student.setLastName(getElementValue(studentElement, "LastName"));
        student.setGender(getElementValue(studentElement, "Gender"));

        // Parse GPA as a string
        String gpaStr = getElementValue(studentElement, "GPA");
        if (!gpaStr.isEmpty()) {
            student.setGpa(Double.parseDouble(gpaStr));
        }

        // Parse Level as a string
        String levelStr = getElementValue(studentElement, "Level");
        if (!levelStr.isEmpty()) {
            student.setLevel(Integer.parseInt(levelStr));
        }

        student.setAddress(getElementValue(studentElement, "Address"));
        return student;
    }


    public String getElementValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    @PostMapping("/deleteById")
    public String deleteById(@RequestParam String studentId, Model model) {
        try {
            File file = new File("data.xml");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Normalize the document
            document.getDocumentElement().normalize();

            NodeList studentList = document.getElementsByTagName("Student");

            for (int i = 0; i < studentList.getLength(); i++) {
                Node studentNode = studentList.item(i);

                if (studentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element studentElement = (Element) studentNode;

                    // Get the ID attribute of the current student
                    String id = studentElement.getAttribute("ID");

                    // Check if the current student's ID matches the search ID
                    if (id.equals(studentId)) {
                        // Remove the student node from the XML document
                        studentNode.getParentNode().removeChild(studentNode);

                        // Save the updated XML document
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(document);
                        StreamResult result = new StreamResult(file);
                        transformer.transform(source, result);

                        model.addAttribute("result", "Student deleted successfully!");
                        return "studentDeleted";
                    }
                }
            }

            model.addAttribute("result", "Student not found!");
            return "notFound";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("result", "Error occurred!");
            return "error";
        }
    }

    @GetMapping("/sortFile")
    public String sortData(@RequestParam String attributeName, @RequestParam String sortOrder, Model model) throws Exception {
        List<Student> sortedList = getAllStudentsFromXML();

        // Define a comparator based on the attribute name
        Comparator<Student> comparator = null;

        switch (attributeName.toLowerCase()) {
            case "id":
                comparator = Comparator.comparingInt(student -> Integer.parseInt(student.getId()));
                break;
            case "firstname":
                comparator = Comparator.comparing(Student::getFirstName);
                break;
            case "lastname":
                comparator = Comparator.comparing(Student::getLastName);
                break;
            case "gpa":
                comparator = Comparator.comparingDouble(Student::getGpa);
                break;
            case "level":
                comparator = Comparator.comparingInt(Student::getLevel);
                break;
            case "address":
                comparator = Comparator.comparing(Student::getAddress);
                break;
            // Add more cases for other attributes as needed

            default:
                // Handle invalid attribute name
                model.addAttribute("sortError", "Invalid attribute name for sorting.");
                return "sort-result";
        }

        // Sort the list based on the comparator
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sortedList.sort(Collections.reverseOrder(comparator));
        } else {
            sortedList.sort(comparator);
        }

        // Update the XML file with the sorted data
        updateXmlFile(sortedList);

        model.addAttribute("sortedList", sortedList);
        model.addAttribute("attributeName", attributeName);
        model.addAttribute("sortOrder", sortOrder);

        return "sort-result";
    }

    private void updateXmlFile(List<Student> sortedList) {
        try {
            File xmlFile = new File("data.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(xmlFile);

            // Create a new root element if it doesn't exist
            Element root = document.getDocumentElement();
            if (root == null) {
                root = document.createElement("University");
                document.appendChild(root);
            }

            // Clear existing student records in the new root element
            NodeList nodeList = root.getChildNodes();
            for (int i = nodeList.getLength() - 1; i >= 0; i--) {
                Node node = nodeList.item(i);
                root.removeChild(node);
            }

            // Append the sorted student records to the new root element
            for (Student student : sortedList) {
                Element studentRecord = document.createElement("Student");

                // Populate studentRecord with attributes
                studentRecord.setAttribute("ID", String.valueOf(student.getId()));
                Element firstname = document.createElement("FirstName");
                firstname.appendChild(document.createTextNode(student.getFirstName()));
                studentRecord.appendChild(firstname);
                // Add more elements for other attributes as needed

                Element lastName = document.createElement("LastName");
                lastName.appendChild( document.createTextNode(student.getLastName()));
                studentRecord.appendChild(lastName);
                Element gender = document.createElement("Gender");
                gender.appendChild(document.createTextNode(student.getGender()));
                studentRecord.appendChild(gender);
                Element gpa = document.createElement("GPA");
                gpa.appendChild(document.createTextNode(String.valueOf(student.getGpa())));
                studentRecord.appendChild(gpa);
                Element level = document.createElement("Level");
                level.appendChild(document.createTextNode(String.valueOf(student.getLevel())));
                studentRecord.appendChild(level);
                Element address = document.createElement("Address");
                address.appendChild( document.createTextNode(String.valueOf(student.getAddress())));
                studentRecord.appendChild(address);
                root.appendChild(studentRecord);
            }

            // Save the updated XML document
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

}
