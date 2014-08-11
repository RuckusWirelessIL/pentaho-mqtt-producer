package com.ruckuswireless.pentaho.mqtt.producer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Holds data processed by this step
 * 
 * @author Michael
 */
public class MQTTProducerData extends BaseStepData implements StepDataInterface {

	MqttClient client;
	RowMetaInterface outputRowMeta;
	int inputFieldNr;
	ValueMetaInterface inputFieldMeta;
}
