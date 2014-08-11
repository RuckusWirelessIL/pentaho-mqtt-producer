package com.ruckuswireless.pentaho.mqtt.producer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * UI for the MQTT Client step
 * 
 * @author Michael Spector
 */
public class MQTTProducerDialog extends BaseStepDialog implements
		StepDialogInterface {

	private MQTTProducerMeta producerMeta;
	private TextVar wBroker;
	private TextVar wTopicName;
	private TextVar wClientID;
	private TextVar wTimeout;
	private TextVar wQOS;
	private CCombo wInputField;

	public MQTTProducerDialog(Shell parent, Object in, TransMeta tr, String sname) {
		super(parent, (BaseStepMeta) in, tr, sname);
		producerMeta = (MQTTProducerMeta) in;
	}

	public MQTTProducerDialog(Shell parent, BaseStepMeta baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, baseStepMeta, transMeta, stepname);
		producerMeta = (MQTTProducerMeta) baseStepMeta;
	}

	public MQTTProducerDialog(Shell parent, int nr, BaseStepMeta in, TransMeta tr) {
		super(parent, nr, in, tr);
		producerMeta = (MQTTProducerMeta) in;
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
				| SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, producerMeta);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				producerMeta.setChanged();
			}
		};
		changed = producerMeta.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("MQTTClientDialog.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Step name
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages
				.getString("MQTTClientDialog.StepName.Label"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		Control lastControl = wStepname;

		// Broker URL
		Label wlBroker = new Label(shell, SWT.RIGHT);
		wlBroker.setText(Messages.getString("MQTTClientDialog.Broker.Label"));
		props.setLook(wlBroker);
		FormData fdlBroker = new FormData();
		fdlBroker.top = new FormAttachment(lastControl, margin);
		fdlBroker.left = new FormAttachment(0, 0);
		fdlBroker.right = new FormAttachment(middle, -margin);
		wlBroker.setLayoutData(fdlBroker);
		wBroker = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wBroker);
		wBroker.addModifyListener(lsMod);
		FormData fdBroker = new FormData();
		fdBroker.top = new FormAttachment(lastControl, margin);
		fdBroker.left = new FormAttachment(middle, 0);
		fdBroker.right = new FormAttachment(100, 0);
		wBroker.setLayoutData(fdBroker);
		lastControl = wBroker;

		// Topic name
		Label wlTopicName = new Label(shell, SWT.RIGHT);
		wlTopicName.setText(Messages
				.getString("MQTTClientDialog.TopicName.Label"));
		props.setLook(wlTopicName);
		FormData fdlTopicName = new FormData();
		fdlTopicName.top = new FormAttachment(lastControl, margin);
		fdlTopicName.left = new FormAttachment(0, 0);
		fdlTopicName.right = new FormAttachment(middle, -margin);
		wlTopicName.setLayoutData(fdlTopicName);
		wTopicName = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wTopicName);
		wTopicName.addModifyListener(lsMod);
		FormData fdTopicName = new FormData();
		fdTopicName.top = new FormAttachment(lastControl, margin);
		fdTopicName.left = new FormAttachment(middle, 0);
		fdTopicName.right = new FormAttachment(100, 0);
		wTopicName.setLayoutData(fdTopicName);
		lastControl = wTopicName;

		// Client ID
		Label wlClientID = new Label(shell, SWT.RIGHT);
		wlClientID.setText(Messages
				.getString("MQTTClientDialog.ClientID.Label"));
		props.setLook(wlClientID);
		FormData fdlClientID = new FormData();
		fdlClientID.top = new FormAttachment(lastControl, margin);
		fdlClientID.left = new FormAttachment(0, 0);
		fdlClientID.right = new FormAttachment(middle, -margin);
		wlClientID.setLayoutData(fdlClientID);
		wClientID = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wClientID);
		wClientID.addModifyListener(lsMod);
		FormData fdClientID = new FormData();
		fdClientID.top = new FormAttachment(lastControl, margin);
		fdClientID.left = new FormAttachment(middle, 0);
		fdClientID.right = new FormAttachment(100, 0);
		wClientID.setLayoutData(fdClientID);
		lastControl = wClientID;

		// Connection timeout
		Label wlConnectionTimeout = new Label(shell, SWT.RIGHT);
		wlConnectionTimeout.setText(Messages
				.getString("MQTTClientDialog.ConnectionTimeout.Label"));
		props.setLook(wlConnectionTimeout);
		FormData fdlConnectionTimeout = new FormData();
		fdlConnectionTimeout.top = new FormAttachment(lastControl, margin);
		fdlConnectionTimeout.left = new FormAttachment(0, 0);
		fdlConnectionTimeout.right = new FormAttachment(middle, -margin);
		wlConnectionTimeout.setLayoutData(fdlConnectionTimeout);
		wTimeout = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wTimeout);
		wTimeout.addModifyListener(lsMod);
		FormData fdConnectionTimeout = new FormData();
		fdConnectionTimeout.top = new FormAttachment(lastControl, margin);
		fdConnectionTimeout.left = new FormAttachment(middle, 0);
		fdConnectionTimeout.right = new FormAttachment(100, 0);
		wTimeout.setLayoutData(fdConnectionTimeout);
		lastControl = wTimeout;

		// QOS
		Label wlQOS = new Label(shell, SWT.RIGHT);
		wlQOS.setText(Messages.getString("MQTTClientDialog.QOS.Label"));
		props.setLook(wlQOS);
		FormData fdlQOS = new FormData();
		fdlQOS.top = new FormAttachment(lastControl, margin);
		fdlQOS.left = new FormAttachment(0, 0);
		fdlQOS.right = new FormAttachment(middle, -margin);
		wlQOS.setLayoutData(fdlQOS);
		wQOS = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wQOS);
		wQOS.addModifyListener(lsMod);
		FormData fdQOS = new FormData();
		fdQOS.top = new FormAttachment(lastControl, margin);
		fdQOS.left = new FormAttachment(middle, 0);
		fdQOS.right = new FormAttachment(100, 0);
		wQOS.setLayoutData(fdQOS);
		lastControl = wQOS;

		// Input field
		RowMetaInterface previousFields;
		try {
			previousFields = transMeta.getPrevStepFields(stepMeta);
		} catch (KettleStepException e) {
			new ErrorDialog(
					shell,
					BaseMessages.getString("System.Dialog.Error.Title"),
					Messages.getString("MQTTClientDialog.ErrorDialog.UnableToGetInputFields.Message"),
					e);
			previousFields = new RowMeta();
		}
		Label wlInputField = new Label(shell, SWT.RIGHT);
		wlInputField.setText(Messages
				.getString("MQTTClientDialog.FieldName.Label"));
		props.setLook(wlInputField);
		FormData fdlInputField = new FormData();
		fdlInputField.top = new FormAttachment(lastControl, margin);
		fdlInputField.left = new FormAttachment(0, 0);
		fdlInputField.right = new FormAttachment(middle, -margin);
		wlInputField.setLayoutData(fdlInputField);
		wInputField = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wInputField.setItems(previousFields.getFieldNames());
		props.setLook(wInputField);
		wInputField.addModifyListener(lsMod);
		FormData fdFilename = new FormData();
		fdFilename.top = new FormAttachment(lastControl, margin);
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right = new FormAttachment(100, 0);
		wInputField.setLayoutData(fdFilename);
		lastControl = wInputField;

		// Buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};
		wStepname.addSelectionListener(lsDef);
		wTopicName.addSelectionListener(lsDef);
		wInputField.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize(shell, 440, 350, true);

		getData(producerMeta, true);
		producerMeta.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	private void getData(MQTTProducerMeta producerMeta, boolean copyStepname) {
		if (copyStepname) {
			wStepname.setText(stepname);
		}
		wBroker.setText(Const.NVL(producerMeta.getBroker(), ""));
		wTopicName.setText(Const.NVL(producerMeta.getTopic(), ""));
		wInputField.setText(Const.NVL(producerMeta.getField(), ""));
		wClientID.setText(Const.NVL(producerMeta.getClientId(), ""));
		wTimeout.setText(Const.NVL(producerMeta.getTimeout(), "10000"));
		wQOS.setText(Const.NVL(producerMeta.getQoS(), "0"));
		wStepname.selectAll();
	}

	private void cancel() {
		stepname = null;
		producerMeta.setChanged(changed);
		dispose();
	}

	/**
	 * Copy information from the dialog fields to the meta-data input
	 */
	private void setData(MQTTProducerMeta producerMeta) {
		producerMeta.setBroker(wBroker.getText());
		producerMeta.setTopic(wTopicName.getText());
		producerMeta.setField(wInputField.getText());
		producerMeta.setClientId(wClientID.getText());
		producerMeta.setTimeout(wTimeout.getText());
		producerMeta.setQoS(wQOS.getText());
		producerMeta.setChanged();
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText())) {
			return;
		}
		setData(producerMeta);
		stepname = wStepname.getText();
		dispose();
	}
}
