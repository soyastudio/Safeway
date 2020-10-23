package soya.framework.tools.xmlbeans;

import soya.framework.tools.util.StringBuilderUtils;

import java.util.List;

public class XmlConstructTreeRenderer extends XmlConstructTree {

    public static final String ROOT_PREFIX = "root://";
    public static final String CREATE_PREFIX = "create://";
    public static final String ASSIGN_PREFIX = "assign://";
    public static final String CONSTRUCT_PREFIX = "construct://";
    public static final String LOOP_PREFIX = "loop://";
    public static final String CONSTRUCTOR_PREFIX = "constructor://";
    public static final String PROCEDURE_PREFIX = "procedure://";
    public static final String PARAM_PREFIX = "param://";

    @Override
    public String render(XmlSchemaBase base) {
        StringBuilder builder = new StringBuilder();
        XmlSchemaBase.MappingNode root = base.getRoot();
        printNode(root, builder, 0);

        return builder.toString();
    }

    protected void printNode(XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {
        if (!isMapped(node)) {
            return;
        }

        if (node.getNodeType().equals(XmlSchemaBase.NodeType.Folder)) {
            if (node.getAnnotation(CONSTRUCT) != null) {
                ConstructTree tree = new ConstructTree(node);
                Construct construct = tree.getConstruct();
                StringBuilderUtils.println(CONSTRUCT_PREFIX + node.getName() + ":" + " # " + node.getPath(), builder, node.getLevel() + indent);

                if (construct.procedure != null) {
                    Procedure procedure = construct.procedure;
                    StringBuilderUtils.println(PROCEDURE_PREFIX + procedure.name + ":", builder, node.getLevel() + indent + 1);
                    procedure.parameters.forEach(e -> {
                        StringBuilderUtils.println("- " + PARAM_PREFIX + e.name, builder, node.getLevel() + indent + 2);
                    });

                    StringBuilderUtils.println(builder);

                } else {
                    tree.getLoopTree().entrySet().forEach(e -> {
                        WhileLoop loop = e.getValue().getObject();
                        StringBuilderUtils.println("- " + LOOP_PREFIX + loop.name + ":", builder, node.getLevel() + indent + 1);
                        List<XmlSchemaBase.MappingNode> l = e.getValue().getNodes();
                        l.forEach(ln -> {
                            printNode(ln, builder, indent + 1);
                        });

                        StringBuilderUtils.println(builder);
                    });

                    for (Constructor e : construct.constructors) {
                        StringBuilderUtils.println("- " + CONSTRUCTOR_PREFIX + e.name + ":", builder, node.getLevel() + indent + 1);


                        StringBuilderUtils.println(builder);
                    }
                }

            } else {
                StringBuilderUtils.println(CREATE_PREFIX + node.getName() + ":" + " # " + node.getPath(), builder, node.getLevel() + indent);
                if (node.getAnnotation(MAPPING) != null) {
                    Mapping mapping = getMapping(node);
                    String assignment = getAssignment(mapping);
                    StringBuilderUtils.println("assignment: " + assignment, builder, node.getLevel() + indent + 1);
                    StringBuilderUtils.println(builder);

                } else {
                    for (XmlSchemaBase.MappingNode child : node.getChildren()) {
                        printNode(child, builder, indent);
                    }
                }
            }


        } else {
            StringBuilderUtils.println(ASSIGN_PREFIX + node.getName() + ":" + " # " + node.getPath(), builder, node.getLevel() + indent);
            Mapping mapping = getMapping(node);
            if (mapping != null) {
                if (mapping.mappingRule != null) {
                    //StringBuilderUtils.println("mapping: " + mapping.mappingRule, builder, node.getLevel() + indent + 1);
                }

                if (mapping.sourcePath != null) {
                    //StringBuilderUtils.println("source: " + mapping.sourcePath, builder, node.getLevel() + indent + 1);
                }

                if (mapping.assignment != null) {
                    String assignment = getAssignment(mapping);
                    StringBuilderUtils.println("assignment: " + assignment, builder, node.getLevel() + indent + 1);
                }

                StringBuilderUtils.println(builder);
            }
        }
    }

    private String getAssignment(Mapping mapping) {
        if (mapping == null) {
            return null;
        }

        if (mapping.assignment == null) {
            return "'???'";

        } else if (mapping.assignment.contains("'")) {
            return "\"" + mapping.assignment + "\"";

        } else {
            return mapping.assignment;
        }

    }
}
