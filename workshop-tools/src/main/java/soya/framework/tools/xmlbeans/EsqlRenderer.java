package soya.framework.tools.xmlbeans;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import soya.framework.tools.util.StringBuilderUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class EsqlRenderer extends XmlSchemaBaseRenderer implements MappingFeature {
    public static final String URI = "http://collab.safeway.com/it/architecture/info/default.aspx";
    public static final String DOCUMENT_ROOT = "xmlDocRoot";

    private static Gson GSON = new Gson();

    private String brokerSchema;
    private String moduleName = "MODULE_NAME";

    private String inputRootVariable = "_inputRoot";
    private String inputRootReference;

    private XmlSchemaBase base;
    private Map<String, WhileLoop> loopMap = new LinkedHashMap<>();

    public void setBrokerSchema(String brokerSchema) {
        this.brokerSchema = brokerSchema;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public String render(XmlSchemaBase base) {
        this.base = base;

        StringBuilder builder = new StringBuilder();
        if (brokerSchema != null && brokerSchema.trim().length() > 0) {
            builder.append("BROKER SCHEMA ").append(brokerSchema.trim()).append("\n\n");
        }

        builder.append("CREATE COMPUTE MODULE ").append(moduleName);
        StringBuilderUtils.println(builder, 2);

        // UDP:
        Variable[] variables = base.getAnnotation(GLOBAL_VARIABLE, Variable[].class);
        if (variables != null && variables.length > 0) {
            StringBuilderUtils.println("-- Declare UDPs", builder, 1);
            for (Variable v : variables) {
                StringBuilderUtils.println("DECLARE " + v.name + " EXTERNAL " + v.type + " " + v.defaultValue + ";", builder, 1);
            }

            StringBuilderUtils.println(builder);
        }

        StringBuilderUtils.println("CREATE FUNCTION Main() RETURNS BOOLEAN", builder, 1);
        begin(builder, 1);

        declareInputRoot(builder);
        declareNamespace(builder);

        // Declare Output Domain
        StringBuilderUtils.indent(builder, 2);
        builder.append("-- Declare Output Message Root").append("\n");
        StringBuilderUtils.indent(builder, 2);
        builder.append("CREATE LASTCHILD OF OutputRoot DOMAIN ").append("'XMLNSC'").append(";\n\n");
        StringBuilderUtils.indent(builder, 2);
        builder.append("DECLARE xmlDocRoot REFERENCE TO OutputRoot.XMLNSC.").append(base.getRoot().getName()).append(";\n");
        StringBuilderUtils.indent(builder, 2);
        builder.append("CREATE LASTCHILD OF OutputRoot.").append("XMLNSC AS ").append(DOCUMENT_ROOT).append(" TYPE XMLNSC.Folder NAME '").append(base.getRoot().getName()).append("'").append(";\n");
        StringBuilderUtils.println("SET OutputRoot.XMLNSC." + base.getRoot().getName() + ".(XMLNSC.NamespaceDecl)xmlns:Abs=Abs;",
                builder, 2);
        StringBuilderUtils.println(builder);

        printNode(base.getRoot(), builder);

        StringBuilderUtils.println("RETURN TRUE;", builder, 2);
        StringBuilderUtils.println("END;", builder, 1);
        StringBuilderUtils.println(builder);

        builder.append("END MODULE;");

        return builder.toString();
    }

    private void begin(StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }
        builder.append("BEGIN").append("\n");
    }

    private void declareInputRoot(StringBuilder builder) {
        StringBuilderUtils.println("-- Declare Input Message Root", builder, 2);
        StringBuilderUtils.println("DECLARE " + inputRootVariable + " REFERENCE TO " + inputRootReference + ";", builder, 2);
        StringBuilderUtils.println(builder);
    }

    private void declareNamespace(StringBuilder builder) {
        StringBuilderUtils.println("-- Declare Namespace", builder, 2);
        StringBuilderUtils.println("DECLARE " + "Abs" + " NAMESPACE " + "'https://collab.safeway.com/it/architecture/info/default.aspx'" + ";", builder, 2);
        StringBuilderUtils.println(builder);
    }

    private void printNode(XmlSchemaBase.MappingNode node, StringBuilder builder) {
        if (XmlSchemaBase.NodeType.Folder.equals(node.getNodeType())) {
            if (node.getAnnotation(MAPPED) == null) {
                // do nothing:

            } else if (node.getAnnotation(BLOCK) != null) {
                printBlock(node, builder);

            } else if (node.getAnnotation(MAPPINGS) != null) {
                printConstruction(node, builder);

            } else if (node.getAnnotation(LOOP) != null) {
                printLoop(node, builder);

            } else {
                printSimpleFolder(node, builder);
            }
        } else {
            printLeaf(node, builder);
        }
    }

    private void printBlock(XmlSchemaBase.MappingNode node, StringBuilder builder) {
        String path = node.getPath();
        StringBuilderUtils.println("-- " + path, builder, node.getLevel());
        StringBuilderUtils.println("DECLARE " + node.getAlias() + " REFERENCE TO " + node.getParent().getAlias() + ";", builder, node.getLevel());
        StringBuilderUtils.println("CREATE LASTCHILD OF " + node.getParent().getAlias() + " AS " + node.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                , builder, node.getLevel());
        StringBuilderUtils.println(builder);

        JsonObject jsonObject = node.getAnnotation(BLOCK, JsonObject.class);
        jsonObject.entrySet().forEach(e -> {
            String key = e.getKey();
            JsonElement jsonElement = e.getValue();
            if (!key.startsWith("_")) {
                String childPath = path + "/" + key;
                XmlSchemaBase.MappingNode child = base.get(childPath);
                if (jsonElement.isJsonPrimitive()) {
                    String assignment = jsonElement.getAsJsonPrimitive().getAsString();

                    if (assignment.startsWith(Function.CHAIN)) {

                    } else {
                        assignment = assignment.replace("$", inputRootVariable);
                        StringBuilderUtils.println("-- " + child.getPath(), builder, child.getLevel());
                        if (!key.startsWith("@")) {
                            // Field:
                            StringBuilderUtils.println("SET " + node.getAlias() + ".(XMLNSC.Field)" + getFullName(child) + " = " + assignment + ";", builder, child.getLevel());
                        } else {
                            // Attribute:
                            StringBuilderUtils.println("SET " + node.getAlias() + ".(XMLNSC.Attribute)" + getFullName(child) + " = " + assignment + ";", builder, child.getLevel());
                        }
                    }
                    StringBuilderUtils.println(builder);

                } else if (jsonElement.isJsonObject()) {


                }

            } else if (key.equals("_while")) {
                JsonObject whileLoop = jsonElement.getAsJsonObject();
                WhileLoop wl = GSON.fromJson(jsonElement, WhileLoop.class);
                JsonObject subBlock = whileLoop.get(BLOCK).getAsJsonObject();

                StringBuilderUtils.println("-- BUILD FROM " + wl.sourcePath + " TO " + node.getPath() + ":", builder, node.getLevel() + 1);
                String assignment = getAssignment(wl, inputRootVariable);
                StringBuilderUtils.println("DECLARE " + wl.variable + " REFERENCE TO " + assignment + ";", builder, node.getLevel() + 1);

                StringBuilderUtils.println(wl.name + " : WHILE LASTMOVE(" + wl.variable + ") DO", builder, node.getLevel() + 1);
                StringBuilderUtils.println(builder);

                printBlock(subBlock, node, builder);

                /*if (XmlSchemaBase.NodeType.Folder.equals(e.getNodeType())) {
                    if (e.getParent() != null) {
                        StringBuilderUtils.println("DECLARE " + e.getAlias() + " REFERENCE TO " + e.getParent().getAlias() + ";", builder, e.getLevel() + 1);
                        StringBuilderUtils.println("CREATE LASTCHILD OF " + e.getParent().getAlias() + " AS " + e.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(e) + "';"
                                , builder, e.getLevel() + 1);
                        StringBuilderUtils.println(builder);

                    }

                    e.getChildren().forEach(n -> {
                        printNode(n, wl, builder);
                    });

                } else if (XmlSchemaBase.NodeType.Field.equals(e.getNodeType())) {
                    StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(e) + " = " + wl.variable + ";", builder, e.getLevel());
                }*/

                StringBuilderUtils.println("MOVE " + wl.variable + " NEXTSIBLING;", builder, node.getLevel() + 1);
                StringBuilderUtils.println("END WHILE " + wl.name + ";", builder, node.getLevel() + 1);
                StringBuilderUtils.println(builder);

            }

        });
    }

    private void printBlock(JsonObject block, XmlSchemaBase.MappingNode node, StringBuilder builder) {
        block.entrySet().forEach(e -> {
            String key = e.getKey();
            JsonElement jsonElement = e.getValue();
            String childPath = node.getPath() + "/" + key;
            XmlSchemaBase.MappingNode child = base.get(childPath);
            if (jsonElement.isJsonPrimitive()) {
                String assignment = jsonElement.getAsJsonPrimitive().getAsString();

                if (assignment.startsWith(Function.CHAIN)) {

                } else {
                    assignment = assignment.replace("$", inputRootVariable);
                    StringBuilderUtils.println("-- " + child.getPath(), builder, child.getLevel() + 1);
                    if (!key.startsWith("@")) {
                        // Field:
                        StringBuilderUtils.println("SET " + node.getAlias() + ".(XMLNSC.Field)" + getFullName(child) + " = " + assignment + ";", builder, child.getLevel() + 1);
                    } else {
                        // Attribute:
                        StringBuilderUtils.println("SET " + node.getAlias() + ".(XMLNSC.Attribute)" + getFullName(child) + " = " + assignment + ";", builder, child.getLevel() + 1);
                    }
                }
                StringBuilderUtils.println(builder);

            } else if (jsonElement.isJsonObject()) {


            }
        });
    }

    private void printConstruction(XmlSchemaBase.MappingNode node, StringBuilder builder) {
        String path = node.getPath();
        StringBuilderUtils.println("-- " + path, builder, node.getLevel());
        StringBuilderUtils.println("DECLARE " + node.getAlias() + " REFERENCE TO " + node.getParent().getAlias() + ";", builder, node.getLevel());
        StringBuilderUtils.println(builder);

        Construction[] constructions = node.getAnnotation(MAPPINGS, Construction[].class);
        for (Construction construction : constructions) {
            StringBuilderUtils.println("-- CONSTRUCT FROM " + construction.from + " TO " + node.getPath(), builder, node.getLevel());
            if (construction.condition != null) {
                StringBuilderUtils.println("IF " + construction.condition + " THEN", builder, node.getLevel());
                StringBuilderUtils.println(builder);
            }

            StringBuilderUtils.println("CREATE LASTCHILD OF " + node.getParent().getAlias() + " AS " + node.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                    , builder, node.getLevel());
            StringBuilderUtils.println(builder);

            JsonObject jsonObject = construction.assignments;
            jsonObject.entrySet().forEach(e -> {
                String propName = e.getKey();
                JsonElement propValue = e.getValue();
                printJsonElement(propName, propValue, node, builder);

            });

            if (construction.condition != null) {
                StringBuilderUtils.println("END IF;", builder, node.getLevel());
            }

            StringBuilderUtils.println(builder);
        }

    }

    private void printJsonElement(String name, JsonElement jsonElement, XmlSchemaBase.MappingNode node, StringBuilder builder) {
        String path = node.getPath() + "/" + name;
        XmlSchemaBase.MappingNode child = base.get(path);
        if (child == null) {
            System.out.println("============= " + path);
        }

        if (jsonElement.isJsonPrimitive()) {
            String assignment = jsonElement.getAsString().replace(INPUT_ROOT, inputRootVariable + ".");
            StringBuilderUtils.println("-- " + path, builder, child.getLevel());
            if (!name.startsWith("@")) {
                // Field:
                StringBuilderUtils.println("SET " + node.getAlias() + ".(XMLNSC.Field)" + getFullName(child) + " = " + assignment + ";", builder, child.getLevel());

            } else {
                // Attribute:
                StringBuilderUtils.println("SET " + node.getAlias() + ".(XMLNSC.Attribute)" + getFullName(child) + " = " + assignment + ";", builder, child.getLevel());

            }

            StringBuilderUtils.println(builder);

        } else if (jsonElement.isJsonObject()) {

            JsonObject sub = jsonElement.getAsJsonObject();
            StringBuilderUtils.println("-- " + path, builder, child.getLevel());
            StringBuilderUtils.println("DECLARE " + child.getAlias() + " REFERENCE TO " + node.getAlias() + ";", builder, child.getLevel());
            StringBuilderUtils.println("CREATE LASTCHILD OF " + node.getAlias() + " AS " + child.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(child) + "';"
                    , builder, child.getLevel());
            StringBuilderUtils.println(builder);

            sub.entrySet().forEach(e -> {
                printJsonElement(e.getKey(), e.getValue(), child, builder);
            });
        }

    }

    private void printLoop(XmlSchemaBase.MappingNode e, StringBuilder builder) {

        JsonArray loops = e.getAnnotation("loop", JsonArray.class);
        loops.forEach(l -> {
            WhileLoop wl = GSON.fromJson(l, WhileLoop.class);
            wl.parent = findParent(wl.sourcePath);
            loopMap.put(wl.sourcePath, wl);

            StringBuilderUtils.println("-- LOOP FROM " + wl.sourcePath + " TO " + e.getPath() + ":", builder, e.getLevel());
            String assignment = getAssignment(wl, inputRootVariable);
            StringBuilderUtils.println("DECLARE " + wl.variable + " REFERENCE TO " + assignment + ";", builder, e.getLevel());

            StringBuilderUtils.println(wl.name + " : WHILE LASTMOVE(" + wl.variable + ") DO", builder, e.getLevel());
            StringBuilderUtils.println(builder);

            if (XmlSchemaBase.NodeType.Folder.equals(e.getNodeType())) {
                if (e.getParent() != null) {
                    StringBuilderUtils.println("DECLARE " + e.getAlias() + " REFERENCE TO " + e.getParent().getAlias() + ";", builder, e.getLevel() + 1);
                    StringBuilderUtils.println("CREATE LASTCHILD OF " + e.getParent().getAlias() + " AS " + e.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(e) + "';"
                            , builder, e.getLevel() + 1);
                    StringBuilderUtils.println(builder);

                }

                e.getChildren().forEach(n -> {
                    printNode(n, wl, builder);
                });

            } else if (XmlSchemaBase.NodeType.Field.equals(e.getNodeType())) {
                StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(e) + " = " + wl.variable + ";", builder, e.getLevel());
            }

            StringBuilderUtils.println("MOVE " + wl.variable + " NEXTSIBLING;", builder, e.getLevel());
            StringBuilderUtils.println("END WHILE " + wl.name + ";", builder, e.getLevel());
            StringBuilderUtils.println(builder);

        });
    }

    private void printSimpleFolder(XmlSchemaBase.MappingNode e, StringBuilder builder) {
        if (e.getParent() != null) {
            StringBuilderUtils.println("-- " + e.getPath(), builder, e.getLevel());
            StringBuilderUtils.println("DECLARE " + e.getAlias() + " REFERENCE TO " + e.getParent().getAlias() + ";", builder, e.getLevel());
            StringBuilderUtils.println("CREATE LASTCHILD OF " + e.getParent().getAlias() + " AS " + e.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(e) + "';"
                    , builder, e.getLevel());
            StringBuilderUtils.println(builder);
        }

        e.getChildren().forEach(n -> {
            printNode(n, builder);
        });
    }

    private void printLeaf(XmlSchemaBase.MappingNode e, StringBuilder builder) {
        Mapping mapping = e.getAnnotation(MAPPING, Mapping.class);
        if (mapping != null && mapping.assignment != null) {
            String assignment = mapping.assignment;

            if (assignment.startsWith(""))

                assignment = getAssignment(mapping, inputRootVariable);
            StringBuilderUtils.println("-- " + e.getPath(), builder, e.getLevel());
            if (XmlSchemaBase.NodeType.Field.equals(e.getNodeType())) {
                // Field:
                StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(e) + " = " + assignment + ";", builder, e.getLevel());

            } else if (XmlSchemaBase.NodeType.Attribute.equals(e.getNodeType())) {
                // Attribute:
                StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Attribute)" + getFullName(e) + " = " + assignment + ";", builder, e.getLevel());

            }

            StringBuilderUtils.println(builder);
        }

    }
/*

    private void printNode2(XmlSchemaBase.MappingNode e, StringBuilder builder) {
        if (!mapped(e)) {
            return;
        }

        Mapping mapping = e.getAnnotation(MAPPING, Mapping.class);
        // if marked as ignore or no mapping for field or attribute
        if (mapping != null && "ignore()".equals(mapping.assignment) || (!XmlSchemaBase.NodeType.Folder.equals(e.getNodeType()) && mapping == null)) {
            return;
        }

        if (e.getAnnotation("loop") != null) {
            JsonArray loops = e.getAnnotation("loop", JsonArray.class);
            loops.forEach(l -> {
                WhileLoop wl = GSON.fromJson(l, WhileLoop.class);
                wl.parent = findParent(wl.sourcePath);
                loopMap.put(wl.sourcePath, wl);

                StringBuilderUtils.println("-- LOOP FROM " + wl.sourcePath + " TO " + e.getPath() + ":", builder, e.getLevel());
                String assignment = getAssignment(wl, inputRootVariable);
                StringBuilderUtils.println("DECLARE " + wl.variable + " REFERENCE TO " + assignment + ";", builder, e.getLevel());

                StringBuilderUtils.println(wl.name + " : WHILE LASTMOVE(" + wl.variable + ") DO", builder, e.getLevel());
                StringBuilderUtils.println(builder);

                if (XmlSchemaBase.NodeType.Folder.equals(e.getNodeType())) {
                    if (e.getParent() != null) {
                        StringBuilderUtils.println("DECLARE " + e.getAlias() + " REFERENCE TO " + e.getParent().getAlias() + ";", builder, e.getLevel() + 1);
                        StringBuilderUtils.println("CREATE LASTCHILD OF " + e.getParent().getAlias() + " AS " + e.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(e) + "';"
                                , builder, e.getLevel() + 1);
                        StringBuilderUtils.println(builder);

                    }

                    e.getChildren().forEach(n -> {
                        printNode(n, wl, builder);
                    });

                } else if (XmlSchemaBase.NodeType.Field.equals(e.getNodeType())) {
                    StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(e) + " = " + wl.variable + ";", builder, e.getLevel());
                }

                StringBuilderUtils.println("MOVE " + wl.variable + " NEXTSIBLING;", builder, e.getLevel());
                StringBuilderUtils.println("END WHILE " + wl.name + ";", builder, e.getLevel());
                StringBuilderUtils.println(builder);

            });

        } else {
            if (XmlSchemaBase.NodeType.Folder.equals(e.getNodeType())) {
                // Folder:
                if (e.getParent() != null) {
                    StringBuilderUtils.println("-- " + e.getPath(), builder, e.getLevel());
                    StringBuilderUtils.println("DECLARE " + e.getAlias() + " REFERENCE TO " + e.getParent().getAlias() + ";", builder, e.getLevel());
                    StringBuilderUtils.println("CREATE LASTCHILD OF " + e.getParent().getAlias() + " AS " + e.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(e) + "';"
                            , builder, e.getLevel());
                    StringBuilderUtils.println(builder);
                }

                e.getChildren().forEach(n -> {
                    printNode(n, builder);
                });

            } else {
                String assignment = getAssignment(mapping, inputRootVariable);
                if (assignment != null) {
                    StringBuilderUtils.println("-- " + e.getPath(), builder, e.getLevel());
                    if (XmlSchemaBase.NodeType.Field.equals(e.getNodeType())) {
                        // Field:
                        StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(e) + " = " + assignment + ";", builder, e.getLevel());

                    } else if (XmlSchemaBase.NodeType.Attribute.equals(e.getNodeType())) {
                        // Attribute:
                        StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Attribute)" + getFullName(e) + " = " + assignment + ";", builder, e.getLevel());

                    }

                    StringBuilderUtils.println(builder);
                }
            }
        }

    }
*/

    private void printNode(XmlSchemaBase.MappingNode e, WhileLoop parentLoop, StringBuilder builder) {
        if (!underWhileLoop(e, parentLoop)) {
            return;
        }

        int depth = parentLoop.getDepth();
        Mapping mapping = e.getAnnotation(MAPPING, Mapping.class);
        if (e.getAnnotation("loop") != null) {
            JsonArray loops = e.getAnnotation("loop", JsonArray.class);
            loops.forEach(l -> {
                WhileLoop wl = GSON.fromJson(l, WhileLoop.class);
                wl.parent = findParent(wl.sourcePath);
                loopMap.put(wl.sourcePath, wl);

                StringBuilderUtils.println("-- LOOP FROM " + wl.sourcePath + " TO " + e.getPath() + ":", builder, e.getLevel() + 1);
                StringBuilderUtils.println("DECLARE " + wl.variable + " REFERENCE TO " + getAssignment(wl, inputRootVariable) + ";", builder, e.getLevel() + 1);

                StringBuilderUtils.println(wl.name + " : WHILE LASTMOVE(" + wl.variable + ") DO", builder, e.getLevel() + 1);
                StringBuilderUtils.println(builder);

                if (XmlSchemaBase.NodeType.Folder.equals(e.getNodeType())) {
                    if (e.getParent() != null) {
                        StringBuilderUtils.println("-- " + e.getPath(), builder, e.getLevel() + 2);
                        StringBuilderUtils.println("DECLARE " + e.getAlias() + " REFERENCE TO " + e.getParent().getAlias() + ";", builder, e.getLevel() + 2);
                        StringBuilderUtils.println("CREATE LASTCHILD OF " + e.getParent().getAlias() + " AS " + e.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(e) + "';"
                                , builder, e.getLevel() + 2);
                        StringBuilderUtils.println(builder);

                    }

                    e.getChildren().forEach(n -> {
                        printNode(n, wl, builder);
                    });

                } else if (XmlSchemaBase.NodeType.Field.equals(e.getNodeType())) {
                    StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(e) + " = " + wl.variable + ";", builder, e.getLevel() + 1);
                }

                StringBuilderUtils.println("MOVE " + wl.variable + " NEXTSIBLING;", builder, e.getLevel() + 1);
                StringBuilderUtils.println("END WHILE " + wl.name + ";", builder, e.getLevel() + 1);
                StringBuilderUtils.println(builder);

            });

        } else {
            if (XmlSchemaBase.NodeType.Folder.equals(e.getNodeType())) {
                if (e.getParent() != null) {
                    StringBuilderUtils.println("-- " + e.getPath(), builder, e.getLevel() + depth);
                    StringBuilderUtils.println("DECLARE " + e.getAlias() + " REFERENCE TO " + e.getParent().getAlias() + ";", builder, e.getLevel() + depth);
                    StringBuilderUtils.println("CREATE LASTCHILD OF " + e.getParent().getAlias() + " AS " + e.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(e) + "';"
                            , builder, e.getLevel() + depth);
                    StringBuilderUtils.println(builder);

                }

                e.getChildren().forEach(n -> {
                    printNode(n, parentLoop, builder);
                });

            } else if (mapping != null) {
                String assignment = getAssignment(mapping, parentLoop);
                if (assignment != null) {
                    StringBuilderUtils.println("-- " + e.getPath(), builder, e.getLevel() + depth);

                    if (XmlSchemaBase.NodeType.Field.equals(e.getNodeType())) {
                        StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(e) + " = " + assignment + ";", builder, e.getLevel() + depth);

                    } else if (XmlSchemaBase.NodeType.Attribute.equals(e.getNodeType())) {
                        StringBuilderUtils.println("SET " + e.getParent().getAlias() + ".(XMLNSC.Attribute)" + getFullName(e) + " = " + assignment + ";", builder, e.getLevel() + depth);

                    }

                    StringBuilderUtils.println(builder);
                }
            }
        }
    }

    private boolean underWhileLoop(XmlSchemaBase.MappingNode node, WhileLoop loop) {
        String sourcePath = loop.sourcePath + "/";

        if(XmlSchemaBase.NodeType.Folder.equals(node.getNodeType())) {
            for(XmlSchemaBase.MappingNode child: node.getChildren()) {
                if(underWhileLoop(child, loop)) {
                    return true;
                }
            }

        } else if(node.getAnnotation(MAPPING) != null) {
            String mappingSource = node.getAnnotation(MAPPING, Mapping.class).sourcePath;
            return mappingSource != null && mappingSource.startsWith(sourcePath);
        }

        return false;
    }

    private String getFullName(XmlSchemaBase.MappingNode node) {
        String fullName = node.getName();
        if (node.getNamespaceURI() != null && node.getNamespaceURI().equals(URI)) {
            fullName = "Abs:" + fullName;
        }

        return fullName;
    }

    private String getAssignment(Mapping mapping, String inputRootVariable) {
        if (mapping == null) {
            return null;
        }

        String assignment = "'???'";
        if (mapping.assignment != null) {
            assignment = mapping.assignment;
            if (assignment.contains(INPUT_ROOT)) {
                assignment = assignment.replace(INPUT_ROOT, inputRootVariable + ".");
            }

        } else if (mapping.sourcePath != null) {
            String path = mapping.sourcePath;
            assignment = inputRootVariable + "." + path.replaceAll("/", "\\.");
        }

        return assignment;
    }

    private String getAssignment(Mapping mapping, WhileLoop loop) {
        String assignment = null;
        if (mapping.assignment != null) {
            assignment = mapping.assignment;

        } else if (mapping.sourcePath != null && mapping.sourcePath.startsWith(loop.sourcePath + "/")) {
            String path = mapping.sourcePath;
            path = path.substring(loop.sourcePath.length() + 1);
            assignment = loop.variable + "." + path.replaceAll("/", "\\.");
        }

        return assignment;
    }

    private WhileLoop findParent(String path) {
        String token = path;
        int index = token.lastIndexOf('/');
        while (index > 0) {
            token = token.substring(0, index);
            if (loopMap.containsKey(token)) {
                return loopMap.get(token);
            }
            index = token.lastIndexOf('/');
        }

        return null;
    }

    private String getAssignment(WhileLoop wl, String inputRoot) {
        if (wl.parent == null) {
            return inputRoot + "." + wl.sourcePath.replace("[*]", "/Item").replaceAll("/", "\\.");

        } else {
            WhileLoop parent = wl.parent;
            String path = wl.sourcePath.substring(parent.sourcePath.length() + 1);
            return wl.parent.variable + "." + path.replace("[*]", "/Item").replaceAll("/", "\\.");

        }

    }
/*
    private boolean mapped(XmlSchemaBase.MappingNode node) {
        if (node.getNodeType().equals(XmlSchemaBase.NodeType.Folder)) {
            for (XmlSchemaBase.MappingNode e : node.getChildren()) {
                if (mapped(e)) {
                    return true;
                }
            }

        } else {
            return node.getAnnotation(MAPPING) != null;
        }

        return false;
    }*/
}
