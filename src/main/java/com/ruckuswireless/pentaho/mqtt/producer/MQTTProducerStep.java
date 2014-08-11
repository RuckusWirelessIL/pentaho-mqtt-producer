package com.ruckuswireless.pentaho.mqtt.producer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * MQTT client step processor
 * 
 * @author Michael Spector
 */
public class MQTTProducerStep extends BaseStep implements StepInterface {

	public MQTTProducerStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		MQTTProducerData data = (MQTTProducerData) sdi;
		if (data.client != null) {
			try {
				if (data.client.isConnected()) {
					data.client.disconnect();
				}
				data.client.close();
				data.client = null;
			} catch (MqttException e) {
				logError(
						Messages.getString("MQTTClientStep.ErrorClosingMQTTClient.Message"),
						e);
			}
		}
		super.dispose(smi, sdi);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		Object[] r = getRow();
		if (r == null) {
			setOutputDone();
			return false;
		}

		MQTTProducerMeta meta = (MQTTProducerMeta) smi;
		MQTTProducerData data = (MQTTProducerData) sdi;

		RowMetaInterface inputRowMeta = getInputRowMeta();

		if (first) {
			first = false;

			// Initialize MQTT client:
			if (data.client == null) {
				String broker = environmentSubstitute(meta.getBroker());
				try {
					data.client = new MqttClient(broker,
							environmentSubstitute(meta.getClientId()));

					MqttConnectOptions connectOptions = new MqttConnectOptions();
					connectOptions.setCleanSession(true);

					String timeout = environmentSubstitute(meta.getTimeout());
					try {
						connectOptions.setConnectionTimeout(Integer
								.parseInt(timeout));
					} catch (NumberFormatException e) {
						throw new KettleException(Messages.getString(
								"MQTTClientStep.WrongTimeoutValue.Message",
								timeout), e);
					}

					logBasic(Messages.getString(
							"MQTTClientStep.CreateMQTTClient.Message", broker));
					data.client.connect(connectOptions);

				} catch (MqttException e) {
					throw new KettleException(Messages.getString(
							"MQTTClientStep.ErrorCreateMQTTClient.Message",
							broker), e);
				}
			}

			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			String inputField = environmentSubstitute(meta.getField());

			int numErrors = 0;
			if (Const.isEmpty(inputField)) {
				logError(Messages
						.getString("MQTTClientStep.Log.FieldNameIsNull")); //$NON-NLS-1$
				numErrors++;
			}
			data.inputFieldNr = inputRowMeta.indexOfValue(inputField);
			if (data.inputFieldNr < 0) {
				logError(Messages.getString(
						"MQTTClientStep.Log.CouldntFindField", inputField)); //$NON-NLS-1$
				numErrors++;
			}
			if (!inputRowMeta.getValueMeta(data.inputFieldNr).isBinary()) {
				logError(Messages.getString(
						"MQTTClientStep.Log.FieldNotValid", inputField)); //$NON-NLS-1$
				numErrors++;
			}
			if (numErrors > 0) {
				setErrors(numErrors);
				stopAll();
				return false;
			}
			data.inputFieldMeta = inputRowMeta.getValueMeta(data.inputFieldNr);
		}

		try {
			byte[] message = data.inputFieldMeta
					.getBinary(r[data.inputFieldNr]);
			String topic = environmentSubstitute(meta.getTopic());

			String qosValue = environmentSubstitute(meta.getQoS());
			int qos;
			try {
				qos = Integer.parseInt(qosValue);
				if (qos != 0 && qos != 1 && qos != 2) {
					throw new KettleException(Messages.getString(
							"MQTTClientStep.WrongQOSValue.Message", qosValue));
				}
			} catch (NumberFormatException e) {
				throw new KettleException(Messages.getString(
						"MQTTClientStep.WrongQOSValue.Message", qosValue), e);
			}

			MqttMessage mqttMessage = new MqttMessage(message);
			mqttMessage.setQos(qos);

			if (isRowLevel()) {
				logRowlevel(Messages.getString(
						"MQTTClientStep.Log.SendingData", topic,
						data.inputFieldMeta.getString(r[data.inputFieldNr])));
			}
			try {
				data.client.publish(topic, mqttMessage);
			} catch (MqttPersistenceException e) {
				throw new KettleException(
						Messages.getString("MQTTClientStep.ErrorPublishing.Message"),
						e);
			} catch (MqttException e) {
				throw new KettleException(
						Messages.getString("MQTTClientStep.ErrorPublishing.Message"),
						e);
			}
		} catch (KettleException e) {
			if (!getStepMeta().isDoingErrorHandling()) {
				logError(Messages.getString(
						"MQTTClientStep.ErrorInStepRunning", e.getMessage()));
				setErrors(1);
				stopAll();
				setOutputDone();
				return false;
			}
			putError(getInputRowMeta(), r, 1, e.toString(), null, getStepname());
		}
		return true;
	}

	public void stopRunning(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {

		MQTTProducerData data = (MQTTProducerData) sdi;
		try {
			if (data.client.isConnected()) {
				data.client.disconnect();
			}
			data.client.close();
			data.client = null;
		} catch (MqttException e) {
			logError(
					Messages.getString("MQTTClientStep.ErrorClosingMQTTClient.Message"),
					e);
		}
		super.stopRunning(smi, sdi);
	}
}
