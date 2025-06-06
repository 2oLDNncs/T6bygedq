package t6bygedq.lib;

import java.io.PrintStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author 2oLDNncs 20250413
 */
public final class MyXMLStreamWriter implements XMLStreamWriter {
	
	private final PrintStream out;
	
	private final XMLStreamWriter delegate;
	
	public MyXMLStreamWriter(final PrintStream out) throws XMLStreamException, FactoryConfigurationError {
		this.out = out;
		this.delegate = XMLOutputFactory.newFactory().createXMLStreamWriter(this.out);
	}
	
	@Override
	public final void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeStartElement(prefix, localName, namespaceURI);
		Chrono.toc("writeStartElement-1");
	}

	@Override
	public final void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeStartElement(namespaceURI, localName);
		Chrono.toc("writeStartElement-2");
	}

	@Override
	public final void writeStartElement(final String localName) throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeStartElement(localName);
		Chrono.toc("writeStartElement-3");
	}

	@Override
	public final void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeStartDocument(version);
		Chrono.toc("writeStartDocument-1");
	}

	@Override
	public final void writeStartDocument(final String version) throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeStartDocument(version);
		Chrono.toc("writeStartDocument-2");
	}

	@Override
	public final void writeStartDocument() throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeStartDocument();
		Chrono.toc("writeStartDocument-3");
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void writeEndElement() throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeEndElement();
		Chrono.toc("writeEndElement");
	}

	@Override
	public final void writeEndDocument() throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeEndDocument();
		Chrono.toc("writeEndDocument");
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void writeCharacters(final String text) throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeCharacters(text);
		Chrono.toc("writeCharacters");
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value)
			throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeAttribute(prefix, namespaceURI, localName, value);
		Chrono.toc("writeAttribute-1");
	}

	@Override
	public final void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException {
		Chrono.tic();
		this.delegate.writeAttribute(namespaceURI, localName, value);
		Chrono.toc("writeAttribute-2");
	}

	@Override
	public final void writeAttribute(final String localName, final String value) throws XMLStreamException {
		Chrono.tic();
//			delegate is way too slow for this method (about 700 000 times slower! WTF)
//			this.delegate.writeAttribute(localName, value);
		this.out.print(String.format(" %s=\"%s\"", localName, escape(value)));
		Chrono.toc("writeAttribute-3");
	}
	
	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws XMLStreamException {
		Log.out(1, "TODO");
		// TODO Auto-generated method stub
		
	}
	
	public static final String escape(final String string) {
		final var result = new StringBuilder();
		
		for (var i = 0; i < string.length(); i += 1) {
			final var c = string.charAt(i);
			
			switch (c) {
			case '"':
				result.append("&quot;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			default:
				result.append(c);
				break;
			}
		}
		
		return result.toString();
	}
	
}