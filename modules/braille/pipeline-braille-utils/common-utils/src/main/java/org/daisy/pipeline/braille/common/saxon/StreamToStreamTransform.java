package org.daisy.pipeline.braille.common.saxon;

import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.evpull.Decomposer;
import net.sf.saxon.evpull.EventIteratorOverSequence;
import net.sf.saxon.evpull.EventToStaxBridge;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.braille.common.TransformationException;

public abstract class StreamToStreamTransform {
	
	private final Configuration configuration;
	
	public StreamToStreamTransform(Configuration configuration) {
		this.configuration = configuration;
	}
	
	protected abstract void _transform(XMLStreamReader reader, Writer writer) throws TransformationException;
	
	public final XdmNode transform(NodeInfo element) throws TransformationException {
		try {
			PipelineConfiguration pipeConfig = new PipelineConfiguration(configuration);
			XMLStreamReader reader
				= new EventToStaxBridge(
					new Decomposer(
						new EventIteratorOverSequence(element.iterate()), pipeConfig), pipeConfig);
			XdmDestination destination = new XdmDestination();
			Receiver receiver = destination.getReceiver(configuration);
			receiver.open();
			WriterImpl writer = new WriterImpl(receiver);
			_transform(reader, writer);
			receiver.close();
			return destination.getXdmNode();
		} catch (Exception e) {
			throw new TransformationException(e);
		}
	}
	
	protected interface Writer {
		public void writeStartElement(QName name) throws XMLStreamException;
		public void writeEndElement() throws XMLStreamException;
		public void writeAttribute(QName name, String value) throws XMLStreamException;
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException;
		public void writeCharacters(String text) throws XMLStreamException;
		public void copyEvent(int event, XMLStreamReader reader) throws XMLStreamException;
		public void copyStartElement(XMLStreamReader reader) throws XMLStreamException;
		public void copyAttributes(XMLStreamReader reader) throws XMLStreamException;
		public void copyText(XMLStreamReader reader) throws XMLStreamException;
		public void copyComment(XMLStreamReader reader) throws XMLStreamException;
		public void copyCData(XMLStreamReader reader) throws XMLStreamException;
		public void copyPI(XMLStreamReader reader) throws XMLStreamException;
	}
	
	protected interface Event {
		public void writeTo(Writer writer) throws XMLStreamException;
	}
	
	private static class WriterImpl extends StreamWriterToReceiver implements Writer {
		
		WriterImpl(Receiver receiver) {
			super(receiver);
		}
		
		public void writeStartElement(QName name) throws XMLStreamException {
			writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
		}
		
		public void writeAttribute(QName name, String value) throws XMLStreamException {
			String prefix = name.getPrefix();
			String ns = name.getNamespaceURI();
			String localPart = name.getLocalPart();
			if (prefix == null || "".equals(prefix))
				writeAttribute(ns, localPart, value);
			else
				writeAttribute(prefix, ns, localPart, value);
		}
		
		public void copyEvent(int event, XMLStreamReader reader) throws XMLStreamException {
			switch (event) {
			case START_DOCUMENT:
				writeStartDocument();
				break;
			case END_DOCUMENT:
				writeEndDocument();
				break;
			case START_ELEMENT:
				copyStartElement(reader);
				break;
			case END_ELEMENT:
				writeEndElement();
				break;
			case SPACE:
			case CHARACTERS:
				copyText(reader);
				break;
			case PROCESSING_INSTRUCTION:
				copyPI(reader);
				break;
			case CDATA:
				copyCData(reader);
				break;
			case COMMENT:
				copyComment(reader);
				break;
			}
		}
		
		public void copyStartElement(XMLStreamReader reader) throws XMLStreamException {
			writeStartElement(reader.getName());
		}
		
		public void copyAttributes(XMLStreamReader reader) throws XMLStreamException {
			for (int i = 0; i < reader.getAttributeCount(); i++)
				writeAttribute(reader.getAttributeName(i), reader.getAttributeValue(i));
		}
		
		public void copyText(XMLStreamReader reader) throws XMLStreamException {
			writeCharacters(reader.getText());
		}
		
		public void copyComment(XMLStreamReader reader) throws XMLStreamException {
			writeComment(reader.getText());
		}
		
		public void copyCData(XMLStreamReader reader) throws XMLStreamException {
			writeCData(reader.getText());
		}
		
		public void copyPI(XMLStreamReader reader) throws XMLStreamException {
			String target = reader.getPITarget();
			String data = reader.getPIData();
			if (data == null)
				writeProcessingInstruction(target);
			else
				writeProcessingInstruction(target, data);
		}
	}
	
	public static abstract class util {
		
		public static abstract class Events {
			
			public static Event startElement(final QName name) {
				return new Event() {
					public void writeTo(Writer writer) throws XMLStreamException {
						writer.writeStartElement(name);
					}
				};
			}
			
			public static Event namespace(final String prefix, final String namespaceURI) {
				return new Event() {
					public void writeTo(Writer writer) throws XMLStreamException {
						writer.writeNamespace(prefix, namespaceURI);
					}
				};
			}
			
			public static Event attribute(final QName name, final String value) {
				return new Event() {
					public void writeTo(Writer writer) throws XMLStreamException {
						writer.writeAttribute(name, value);
					}
				};
			}
			
			public static Event characters(final String text) {
				return new Event() {
					public void writeTo(Writer writer) throws XMLStreamException {
						writer.writeCharacters(text);
					}
				};
			}
			
			public static Event endElement
			= new Event() {
				public void writeTo(Writer writer) throws XMLStreamException {
					writer.writeEndElement();
				}
			};
		}
		
		public static class ToStringWriter implements Writer {
			
			StringBuilder b = new StringBuilder();
			
			Stack<String> elements = new Stack<String>();
			boolean startTagOpen = false;
			
			@Override
			public String toString() {
				return b.toString();
			}
			
			public void writeStartElement(QName name) throws XMLStreamException {
				writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
			}
			
			public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
				if (startTagOpen) {
					b.append(">");
					startTagOpen = false; }
				elements.push(localName);
				b.append("<").append(localName);
				startTagOpen = true;
			}
			
			public void writeEndElement() throws XMLStreamException {
				if (startTagOpen) {
					b.append("/>");
					startTagOpen = false;
					elements.pop(); }
				else
					b.append("</").append(elements.pop()).append(">");
			}
			
			public void writeAttribute(QName name, String value) throws XMLStreamException {
				writeAttribute(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), value);
			}
			
			public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
				b.append(" ").append(localName).append("='").append(value).append("'");
			}
			
			public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {}
			
			public void writeCharacters(String text) throws XMLStreamException {
				if (startTagOpen) {
					b.append(">");
					startTagOpen = false; }
				b.append(text);
			}
			
			public void writeComment(String data) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeProcessingInstruction(String target) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeCData(String data) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			
			// FIXME: how to remove duplication?
			
			public void copyEvent(int event, XMLStreamReader reader) throws XMLStreamException {
				switch (event) {
				case START_ELEMENT:
					copyStartElement(reader);
					break;
				case END_ELEMENT:
					writeEndElement();
					break;
				case SPACE:
				case CHARACTERS:
					copyText(reader);
					break;
				case PROCESSING_INSTRUCTION:
					copyPI(reader);
					break;
				case CDATA:
					copyCData(reader);
					break;
				case COMMENT:
					copyComment(reader);
					break;
				default:
					throw new RuntimeException("unexpected input");
				}
			}
			
			public void copyStartElement(XMLStreamReader reader) throws XMLStreamException {
				writeStartElement(reader.getName());
			}
			
			public void copyAttributes(XMLStreamReader reader) throws XMLStreamException {
				for (int i = 0; i < reader.getAttributeCount(); i++)
					writeAttribute(reader.getAttributeName(i), reader.getAttributeValue(i));
			}
			
			public void copyText(XMLStreamReader reader) throws XMLStreamException {
				writeCharacters(reader.getText());
			}
			
			public void copyComment(XMLStreamReader reader) throws XMLStreamException {
				writeComment(reader.getText());
			}
			
			public void copyCData(XMLStreamReader reader) throws XMLStreamException {
				writeCData(reader.getText());
			}
			
			public void copyPI(XMLStreamReader reader) throws XMLStreamException {
				String target = reader.getPITarget();
				String data = reader.getPIData();
				if (data == null)
					writeProcessingInstruction(target);
				else
					writeProcessingInstruction(target, data);
			}
		}
	}
}
