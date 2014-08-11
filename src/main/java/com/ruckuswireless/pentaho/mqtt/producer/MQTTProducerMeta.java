package com.ruckuswireless.pentaho.mqtt.producer;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * MQTT Client step definitions and serializer to/from XML and to/from Kettle
 * repository.
 * 
 * @author Michael Spector
 */
public class MQTTProducerMeta extends BaseStepMeta implements StepMetaInterface {

	private String broker;
	private String topic;
	private String field;
	private String clientId;
	private String timeout = "10000";
	private String qos = "0";

	/**
	 * @return Broker URL
	 */
	public String getBroker() {
		return broker;
	}

	/**
	 * @param broker
	 *            Broker URL
	 */
	public void setBroker(String broker) {
		this.broker = broker;
	}

	/**
	 * @return MQTT topic name
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic
	 *            MQTT topic name
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return Target field name in Kettle stream
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field
	 *            Target field name in Kettle stream
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return Client ID
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId
	 *            Client ID
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return Connection timeout
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            Connection timeout
	 */
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return QoS to use
	 */
	public String getQoS() {
		return qos;
	}

	/**
	 * @param qos
	 *            QoS to use
	 */
	public void setQoS(String qos) {
		this.qos = qos;
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String input[],
			String output[], RowMetaInterface info) {

		if (broker == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("MQTTClientMeta.Check.InvalidBroker"),
					stepMeta));
		}
		if (topic == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("MQTTClientMeta.Check.InvalidTopic"),
					stepMeta));
		}
		if (field == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("MQTTClientMeta.Check.InvalidField"),
					stepMeta));
		}
		if (clientId == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("MQTTClientMeta.Check.InvalidClientID"),
					stepMeta));
		}
		if (timeout == null) {
			remarks.add(new CheckResult(
					CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("MQTTClientMeta.Check.InvalidConnectionTimeout"),
					stepMeta));
		}
		if (qos == null) {
			remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					Messages.getString("MQTTClientMeta.Check.InvalidQOS"),
					stepMeta));
		}
	}

	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans trans) {
		return new MQTTProducerStep(stepMeta, stepDataInterface, cnr, transMeta,
				trans);
	}

	public StepDataInterface getStepData() {
		return new MQTTProducerData();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {

		try {
			broker = XMLHandler.getTagValue(stepnode, "BROKER");
			topic = XMLHandler.getTagValue(stepnode, "TOPIC");
			field = XMLHandler.getTagValue(stepnode, "FIELD");
			clientId = XMLHandler.getTagValue(stepnode, "CLIENT_ID");
			timeout = XMLHandler.getTagValue(stepnode, "TIMEOUT");
			qos = XMLHandler.getTagValue(stepnode, "QOS");
		} catch (Exception e) {
			throw new KettleXMLException(
					Messages.getString("MQTTClientMeta.Exception.loadXml"), e);
		}
	}

	public String getXML() throws KettleException {
		StringBuilder retval = new StringBuilder();
		if (broker != null) {
			retval.append("    ").append(
					XMLHandler.addTagValue("BROKER", broker));
		}
		if (topic != null) {
			retval.append("    ")
					.append(XMLHandler.addTagValue("TOPIC", topic));
		}
		if (field != null) {
			retval.append("    ")
					.append(XMLHandler.addTagValue("FIELD", field));
		}
		if (clientId != null) {
			retval.append("    ").append(
					XMLHandler.addTagValue("CLIENT_ID", clientId));
		}
		if (timeout != null) {
			retval.append("    ").append(
					XMLHandler.addTagValue("TIMEOUT", timeout));
		}
		if (qos != null) {
			retval.append("    ").append(XMLHandler.addTagValue("QOS", qos));
		}
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId stepId,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try {
			broker = rep.getStepAttributeString(stepId, "BROKER");
			topic = rep.getStepAttributeString(stepId, "TOPIC");
			field = rep.getStepAttributeString(stepId, "FIELD");
			clientId = rep.getStepAttributeString(stepId, "CLIENT_ID");
			timeout = rep.getStepAttributeString(stepId, "TIMEOUT");
			qos = rep.getStepAttributeString(stepId, "QOS");
		} catch (Exception e) {
			throw new KettleException("MQTTClientMeta.Exception.loadRep", e);
		}
	}

	public void saveRep(Repository rep, ObjectId transformationId,
			ObjectId stepId) throws KettleException {
		try {
			if (broker != null) {
				rep.saveStepAttribute(transformationId, stepId, "BROKER",
						broker);
			}
			if (topic != null) {
				rep.saveStepAttribute(transformationId, stepId, "TOPIC", topic);
			}
			if (field != null) {
				rep.saveStepAttribute(transformationId, stepId, "FIELD", field);
			}
			if (clientId != null) {
				rep.saveStepAttribute(transformationId, stepId, "CLIENT_ID",
						clientId);
			}
			if (timeout != null) {
				rep.saveStepAttribute(transformationId, stepId, "TIMEOUT",
						timeout);
			}
			if (qos != null) {
				rep.saveStepAttribute(transformationId, stepId, "QOS", qos);
			}
		} catch (Exception e) {
			throw new KettleException("MQTTClientMeta.Exception.saveRep", e);
		}
	}

	public void setDefault() {
	}
}
