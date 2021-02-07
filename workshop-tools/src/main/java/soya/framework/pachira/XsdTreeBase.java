package soya.framework.pachira;

import org.apache.xmlbeans.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.util.Iterator;

public class XsdTreeBase extends Feature<SchemaTypeSystem> implements TreeBase<SchemaTypeSystem, KnowledgeTree<XsdTreeBase.XsNode>> {

    private KnowledgeTree<XsNode> tree;

    protected XsdTreeBase(SchemaTypeSystem origin) {
        super(origin);
    }

    @Override
    public KnowledgeTree<XsNode> knowledgeBase() {
        return tree;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void main(String[] args) {
        Tree tree = XsdTreeBase.builder()
                .name("Wen Qun")
                .file(new File("C:/github/Workshop/Repository/CMM/BOD/GetCustomerPreferences.xsd"))
                .create().tree;

        Iterator<String> iterator = tree.paths();
        while (iterator.hasNext()) {
            String path = iterator.next();
            TreeNode node = tree.get(path);
            XsNode xsNode = node.getData(XsNode.class);
            System.out.println(path + "=");
        }
    }

    static class Builder implements Barflow.BaselineBuilder<XsdTreeBase> {

        private String name;
        private Object source;
        private SchemaTypeLoader schemaTypeLoader;
        private XmlOptions xmlOptions = new XmlOptions()
                .setErrorListener(null).setCompileDownloadUrls()
                .setCompileNoPvrRule();

        private Barflow.Digester<SchemaTypeSystem, KnowledgeTree> digester = new XsTreeBaseDigester();

        private Builder() {
        }


        @Override
        public Barflow.BaselineBuilder<XsdTreeBase> digester(Barflow.Digester<?, ?> digester) {
            this.digester = (Barflow.Digester<SchemaTypeSystem, KnowledgeTree>) digester;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder schemaTypeLoader(SchemaTypeLoader schemaTypeLoader) {
            this.schemaTypeLoader = schemaTypeLoader;
            return this;
        }

        public Builder xmlOptions(XmlOptions xmlOptions) {
            if (xmlOptions != null) {
                this.xmlOptions = xmlOptions;
            }
            return this;
        }

        public Builder string(String xmlString) {
            this.source = xmlString;
            return this;
        }

        public Builder file(File file) {
            this.source = file;
            return this;
        }

        public Builder url(URL url) {
            this.source = url;
            return this;
        }

        public Builder inputStream(InputStream inputStream) {
            this.source = inputStream;
            return this;
        }

        public Builder reader(Reader reader) {
            this.source = reader;
            return this;
        }

        public Builder xmlStreamReader(XMLStreamReader xmlStreamReader) {
            this.source = xmlStreamReader;
            return this;
        }

        @Override
        public XsdTreeBase create() throws Barflow.FlowBuilderException {
            try {
                // Parse the XML Schema object first to get XML object
                XmlObject parsedSchema = parse(source, xmlOptions);

                // We may have more than schemas to validate with
                XmlObject[] schemas = new XmlObject[]{parsedSchema};

                // Compile the XML Schema to create a schema type system
                SchemaTypeSystem schemaTypeSystem = XmlBeans.compileXsd(schemas, schemaTypeLoader, xmlOptions);

                XsdTreeBase base = new XsdTreeBase(schemaTypeSystem);
                base.tree = this.digester.digest(schemaTypeSystem);

                return base;

            } catch (XmlException | IOException e) {
                throw new Barflow.FlowBuilderException(e);

            }
        }

        private XmlObject parse(Object source, XmlOptions xmlOptions) throws XmlException, IOException {
            if (source == null) {
                throw new IllegalStateException("Source is not set.");

            } else if (source instanceof String) {
                return XmlObject.Factory.parse((String) source, xmlOptions);

            } else if (source instanceof File) {
                return XmlObject.Factory.parse((File) source, xmlOptions);

            } else if (source instanceof URL) {
                return XmlObject.Factory.parse((URL) source, xmlOptions);

            } else if (source instanceof XMLStreamReader) {
                return XmlObject.Factory.parse((XMLStreamReader) source, xmlOptions);

            } else if (source instanceof InputStream) {
                return XmlObject.Factory.parse((InputStream) source, xmlOptions);

            } else if (source instanceof Reader) {
                return XmlObject.Factory.parse((Reader) source, xmlOptions);

            } else {
                throw new IllegalArgumentException("Source type is not supported.");

            }
        }
    }

    static class XsTreeBaseDigester implements Barflow.Digester<SchemaTypeSystem, KnowledgeTree> {

        @Override
        public KnowledgeTree digest(SchemaTypeSystem source) {
            MoneyTree tree = null;
            SchemaType sType = source.documentTypes()[0];
            if (SchemaType.ELEMENT_CONTENT == sType.getContentType()) {

                SchemaLocalElement element = (SchemaLocalElement) sType.getContentModel();
                QName qName = element.getName();

                tree = MoneyTree.newInstance(qName.getLocalPart(), new XsNode(element));
                processParticle(element.getType().getContentModel(), true, tree.root(), tree);
            }

            return tree;
        }

        private void processParticle(SchemaParticle sp, boolean mixed, TreeNode parent, Tree tree) {
            switch (sp.getParticleType()) {
                case (SchemaParticle.ELEMENT):
                    processElement(sp, mixed, parent, tree);
                    break;

                case (SchemaParticle.SEQUENCE):
                    processSequence(sp, mixed, parent, tree);
                    break;

                case (SchemaParticle.CHOICE):
                    processChoice(sp, mixed, parent, tree);
                    break;

                case (SchemaParticle.ALL):
                    processAll(sp, mixed, parent, tree);
                    break;

                case (SchemaParticle.WILDCARD):
                    processWildCard(sp, mixed, parent, tree);
                    break;

                default:
                    // throw new Exception("No Match on Schema Particle Type: " + String.valueOf(sp.getParticleType()));
            }
        }

        private void processElement(SchemaParticle sp, boolean mixed, TreeNode parent, Tree tree) {
            SchemaLocalElement element = (SchemaLocalElement) sp;
            TreeNode treeNode = tree.create(parent, element.getName().getLocalPart(), new XsNode(element));
            SchemaProperty[] attrProps = sp.getType().getAttributeProperties();
            if (attrProps != null) {
                for (SchemaProperty property : attrProps) {
                    tree.create(treeNode, "@" + property.getName().getLocalPart(), new XsNode(property));
                }
            }

            if (element.getType().isSimpleType()) {
                // end
                if (!element.getType().isBuiltinType())
                    System.out.println("===== simple " + element.getName() + ": " + element.getType());

            } else if (element.getType().getContentModel() != null) {
                // next
                processParticle(element.getType().getContentModel(), mixed, treeNode, tree);

            } else {
                if (element.getType().getBaseType() != null) {
                    System.out.println("================== ??? " + element.getName() + ": " + element.getType().getBaseType().isSimpleType());

                }
            }
        }

        private void processSequence(SchemaParticle sp, boolean mixed, TreeNode parent, Tree tree) {
            SchemaParticle[] spc = sp.getParticleChildren();
            for (int i = 0; i < spc.length; i++) {
                processParticle(spc[i], mixed, parent, tree);
            }
        }

        private void processChoice(SchemaParticle sp, boolean mixed, TreeNode parent, Tree tree) {
            System.out.println(sp.getName());

        }

        private void processAll(SchemaParticle sp, boolean mixed, TreeNode parent, Tree tree) {
            System.out.println(sp.getName());
        }

        private void processWildCard(SchemaParticle sp, boolean mixed, TreeNode parent, Tree tree) {
            System.out.println(sp.getName());
        }
    }

    static enum XsNodeType {
        Folder, Field, Attribute
    }

    static class XsNode {
        private transient SchemaType schemaType;

        private QName name;
        private XsNodeType nodeType;

        private BigInteger minOccurs;
        private BigInteger maxOccurs;

        XsNode(SchemaField schemaField) {
            this.schemaType = schemaField.getType();

            this.name = schemaField.getName();
            this.minOccurs = schemaField.getMinOccurs();
            this.minOccurs = schemaField.getMaxOccurs();

            if (schemaField.getType().isSimpleType()) {
                nodeType = XsNodeType.Field;

            } else {
                nodeType = XsNodeType.Folder;

            }

        }

        XsNode(SchemaProperty schemaProperty) {
            this.schemaType = schemaProperty.getType();
            this.nodeType = XsNodeType.Attribute;

            this.name = schemaProperty.getName();
            this.minOccurs = schemaProperty.getMinOccurs();
            this.minOccurs = schemaProperty.getMaxOccurs();
        }
    }

}
