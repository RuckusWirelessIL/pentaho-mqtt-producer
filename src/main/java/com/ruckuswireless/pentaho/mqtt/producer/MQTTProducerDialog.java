package com.ruckuswireless.pentaho.mqtt.producer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
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

	private CCombo wInputField;

	private CTabFolder wTabFolder;

	private CTabItem wGeneralTab;
	private TextVar wBroker;
	private TextVar wTopicName;
	private TextVar wClientID;
	private TextVar wTimeout;
	private TextVar wQOS;

	private CTabItem wCredentialsTab;
	private Button wRequiresAuth;
	private Label wlUsername;
	private TextVar wUsername;
	private Label wlPassword;
	private TextVar wPassword;

	private CTabItem wSSLTab;
	private TextVar wCAFile;
	private TextVar wCertFile;
	private TextVar wKeyFile;
	private TextVar wKeyPassword;

	public MQTTProducerDialog(Shell parent, Object in, TransMeta tr,
			String sname) {
		super(parent, (BaseStepMeta) in, tr, sname);
		producerMeta = (MQTTProducerMeta) in;
	}

	public MQTTProducerDialog(Shell parent, BaseStepMeta baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, baseStepMeta, transMeta, stepname);
		producerMeta = (MQTTProducerMeta) baseStepMeta;
	}

	public MQTTProducerDialog(Shell parent, int nr, BaseStepMeta in,
			TransMeta tr) {
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
		wInputField.setToolTipText(Messages
				.getString("MQTTClientDialog.FieldName.Tooltip"));
		wInputField.setItems(previousFields.getFieldNames());
		props.setLook(wInputField);
		wInputField.addModifyListener(lsMod);
		FormData fdFilename = new FormData();
		fdFilename.top = new FormAttachment(lastControl, margin);
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right = new FormAttachment(100, 0);
		wInputField.setLayoutData(fdFilename);
		lastControl = wInputField;

		// ====================
		// START OF TAB FOLDER
		// ====================
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		// ====================
		// GENERAL TAB
		// ====================

		wGeneralTab = new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages
				.getString("MQTTClientDialog.GeneralTab.Label")); //$NON-NLS-1$

		FormLayout mainLayout = new FormLayout();
		mainLayout.marginWidth = 3;
		mainLayout.marginHeight = 3;

		Composite wGeneralTabComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wGeneralTabComp);
		wGeneralTabComp.setLayout(mainLayout);

		// Broker URL
		Label wlBroker = new Label(wGeneralTabComp, SWT.RIGHT);
		wlBroker.setText(Messages.getString("MQTTClientDialog.Broker.Label"));
		props.setLook(wlBroker);
		FormData fdlBroker = new FormData();
		fdlBroker.top = new FormAttachment(0, margin * 2);
		fdlBroker.left = new FormAttachment(0, 0);
		fdlBroker.right = new FormAttachment(middle, -margin);
		wlBroker.setLayoutData(fdlBroker);
		wBroker = new TextVar(transMeta, wGeneralTabComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wBroker);
		wBroker.addModifyListener(lsMod);
		FormData fdBroker = new FormData();
		fdBroker.top = new FormAttachment(0, margin * 2);
		fdBroker.left = new FormAttachment(middle, 0);
		fdBroker.right = new FormAttachment(100, 0);
		wBroker.setLayoutData(fdBroker);
		lastControl = wBroker;

		// Topic name
		Label wlTopicName = new Label(wGeneralTabComp, SWT.RIGHT);
		wlTopicName.setText(Messages
				.getString("MQTTClientDialog.TopicName.Label"));
		props.setLook(wlTopicName);
		FormData fdlTopicName = new FormData();
		fdlTopicName.top = new FormAttachment(lastControl, margin);
		fdlTopicName.left = new FormAttachment(0, 0);
		fdlTopicName.right = new FormAttachment(middle, -margin);
		wlTopicName.setLayoutData(fdlTopicName);
		wTopicName = new TextVar(transMeta, wGeneralTabComp, SWT.SINGLE
				| SWT.LEFT | SWT.BORDER);
		props.setLook(wTopicName);
		wTopicName.addModifyListener(lsMod);
		FormData fdTopicName = new FormData();
		fdTopicName.top = new FormAttachment(lastControl, margin);
		fdTopicName.left = new FormAttachment(middle, 0);
		fdTopicName.right = new FormAttachment(100, 0);
		wTopicName.setLayoutData(fdTopicName);
		lastControl = wTopicName;

		// Client ID
		Label wlClientID = new Label(wGeneralTabComp, SWT.RIGHT);
		wlClientID.setText(Messages
				.getString("MQTTClientDialog.ClientID.Label"));
		props.setLook(wlClientID);
		FormData fdlClientID = new FormData();
		fdlClientID.top = new FormAttachment(lastControl, margin);
		fdlClientID.left = new FormAttachment(0, 0);
		fdlClientID.right = new FormAttachment(middle, -margin);
		wlClientID.setLayoutData(fdlClientID);
		wClientID = new TextVar(transMeta, wGeneralTabComp, SWT.SINGLE
				| SWT.LEFT | SWT.BORDER);
		props.setLook(wClientID);
		wClientID.addModifyListener(lsMod);
		FormData fdClientID = new FormData();
		fdClientID.top = new FormAttachment(lastControl, margin);
		fdClientID.left = new FormAttachment(middle, 0);
		fdClientID.right = new FormAttachment(100, 0);
		wClientID.setLayoutData(fdClientID);
		lastControl = wClientID;

		// Connection timeout
		Label wlConnectionTimeout = new Label(wGeneralTabComp, SWT.RIGHT);
		wlConnectionTimeout.setText(Messages
				.getString("MQTTClientDialog.ConnectionTimeout.Label"));
		props.setLook(wlConnectionTimeout);
		FormData fdlConnectionTimeout = new FormData();
		fdlConnectionTimeout.top = new FormAttachment(lastControl, margin);
		fdlConnectionTimeout.left = new FormAttachment(0, 0);
		fdlConnectionTimeout.right = new FormAttachment(middle, -margin);
		wlConnectionTimeout.setLayoutData(fdlConnectionTimeout);
		wTimeout = new TextVar(transMeta, wGeneralTabComp, SWT.SINGLE
				| SWT.LEFT | SWT.BORDER);
		props.setLook(wTimeout);
		wTimeout.addModifyListener(lsMod);
		FormData fdConnectionTimeout = new FormData();
		fdConnectionTimeout.top = new FormAttachment(lastControl, margin);
		fdConnectionTimeout.left = new FormAttachment(middle, 0);
		fdConnectionTimeout.right = new FormAttachment(100, 0);
		wTimeout.setLayoutData(fdConnectionTimeout);
		lastControl = wTimeout;

		// QOS
		Label wlQOS = new Label(wGeneralTabComp, SWT.RIGHT);
		wlQOS.setText(Messages.getString("MQTTClientDialog.QOS.Label"));
		props.setLook(wlQOS);
		FormData fdlQOS = new FormData();
		fdlQOS.top = new FormAttachment(lastControl, margin);
		fdlQOS.left = new FormAttachment(0, 0);
		fdlQOS.right = new FormAttachment(middle, -margin);
		wlQOS.setLayoutData(fdlQOS);
		wQOS = new TextVar(transMeta, wGeneralTabComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wQOS);
		wQOS.addModifyListener(lsMod);
		FormData fdQOS = new FormData();
		fdQOS.top = new FormAttachment(lastControl, margin);
		fdQOS.left = new FormAttachment(middle, 0);
		fdQOS.right = new FormAttachment(100, 0);
		wQOS.setLayoutData(fdQOS);
		lastControl = wQOS;

		FormData fdGeneralTabComp = new FormData();
		fdGeneralTabComp.left = new FormAttachment(0, 0);
		fdGeneralTabComp.top = new FormAttachment(0, 0);
		fdGeneralTabComp.right = new FormAttachment(100, 0);
		fdGeneralTabComp.bottom = new FormAttachment(100, 0);
		wGeneralTabComp.setLayoutData(fdGeneralTabComp);

		wGeneralTabComp.layout();
		wGeneralTab.setControl(wGeneralTabComp);

		// ====================
		// CREDENTIALS TAB
		// ====================
		wCredentialsTab = new CTabItem(wTabFolder, SWT.NONE);
		wCredentialsTab.setText(Messages
				.getString("MQTTClientDialog.CredentialsTab.Title")); //$NON-NLS-1$

		Composite wCredentialsComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wCredentialsComp);

		FormLayout fieldsCompLayout = new FormLayout();
		fieldsCompLayout.marginWidth = Const.FORM_MARGIN;
		fieldsCompLayout.marginHeight = Const.FORM_MARGIN;
		wCredentialsComp.setLayout(fieldsCompLayout);

		Label wlRequiresAuth = new Label(wCredentialsComp, SWT.RIGHT);
		wlRequiresAuth.setText(Messages
				.getString("MQTTClientDialog.RequireAuth.Label"));
		props.setLook(wlRequiresAuth);
		FormData fdlRequriesAuth = new FormData();
		fdlRequriesAuth.left = new FormAttachment(0, 0);
		fdlRequriesAuth.top = new FormAttachment(0, margin * 2);
		fdlRequriesAuth.right = new FormAttachment(middle, -margin);
		wlRequiresAuth.setLayoutData(fdlRequriesAuth);
		wRequiresAuth = new Button(wCredentialsComp, SWT.CHECK);
		props.setLook(wRequiresAuth);
		FormData fdRequiresAuth = new FormData();
		fdRequiresAuth.left = new FormAttachment(middle, 0);
		fdRequiresAuth.top = new FormAttachment(0, margin * 2);
		fdRequiresAuth.right = new FormAttachment(100, 0);
		wRequiresAuth.setLayoutData(fdRequiresAuth);

		wRequiresAuth.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				boolean enabled = wRequiresAuth.getSelection();
				wlUsername.setEnabled(enabled);
				wUsername.setEnabled(enabled);
				wlPassword.setEnabled(enabled);
				wPassword.setEnabled(enabled);
			}
		});
		lastControl = wRequiresAuth;

		// Username field
		wlUsername = new Label(wCredentialsComp, SWT.RIGHT);
		wlUsername.setEnabled(false);
		wlUsername.setText(Messages
				.getString("MQTTClientDialog.Username.Label")); //$NON-NLS-1$
		props.setLook(wlUsername);
		FormData fdlUsername = new FormData();
		fdlUsername.left = new FormAttachment(0, -margin);
		fdlUsername.right = new FormAttachment(middle, -2 * margin);
		fdlUsername.top = new FormAttachment(lastControl, 2 * margin);
		wlUsername.setLayoutData(fdlUsername);

		wUsername = new TextVar(transMeta, wCredentialsComp, SWT.SINGLE
				| SWT.LEFT | SWT.BORDER);
		wUsername.setEnabled(false);
		wUsername.setToolTipText(Messages
				.getString("MQTTClientDialog.Username.Tooltip"));
		props.setLook(wUsername);
		wUsername.addModifyListener(lsMod);
		FormData fdResult = new FormData();
		fdResult.left = new FormAttachment(middle, -margin);
		fdResult.top = new FormAttachment(lastControl, 2 * margin);
		fdResult.right = new FormAttachment(100, 0);
		wUsername.setLayoutData(fdResult);
		lastControl = wUsername;

		// Password field
		wlPassword = new Label(wCredentialsComp, SWT.RIGHT);
		wlPassword.setEnabled(false);
		wlPassword.setText(Messages
				.getString("MQTTClientDialog.Password.Label")); //$NON-NLS-1$
		props.setLook(wlPassword);
		FormData fdlPassword = new FormData();
		fdlPassword.left = new FormAttachment(0, -margin);
		fdlPassword.right = new FormAttachment(middle, -2 * margin);
		fdlPassword.top = new FormAttachment(lastControl, margin);
		wlPassword.setLayoutData(fdlPassword);

		wPassword = new TextVar(transMeta, wCredentialsComp, SWT.SINGLE
				| SWT.LEFT | SWT.BORDER | SWT.PASSWORD);
		wPassword.setEnabled(false);
		wPassword.setToolTipText(Messages
				.getString("MQTTClientDialog.Password.Tooltip"));
		props.setLook(wPassword);
		wPassword.addModifyListener(lsMod);
		FormData fdPassword = new FormData();
		fdPassword.left = new FormAttachment(middle, -margin);
		fdPassword.top = new FormAttachment(lastControl, margin);
		fdPassword.right = new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);

		FormData fdCredentialsComp = new FormData();
		fdCredentialsComp.left = new FormAttachment(0, 0);
		fdCredentialsComp.top = new FormAttachment(0, 0);
		fdCredentialsComp.right = new FormAttachment(100, 0);
		fdCredentialsComp.bottom = new FormAttachment(100, 0);
		wCredentialsComp.setLayoutData(fdCredentialsComp);

		wCredentialsComp.layout();
		wCredentialsTab.setControl(wCredentialsComp);

		// ====================
		// SSL TAB
		// ====================
		wSSLTab = new CTabItem(wTabFolder, SWT.NONE);
		wSSLTab.setText(Messages.getString("MQTTClientDialog.SSLTab.Label")); //$NON-NLS-1$

		Composite wSSLComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wSSLComp);

		FormLayout sslCompLayout = new FormLayout();
		sslCompLayout.marginWidth = Const.FORM_MARGIN;
		sslCompLayout.marginHeight = Const.FORM_MARGIN;
		wSSLComp.setLayout(sslCompLayout);

		// Server CA file path
		Label wlCAFile = new Label(wSSLComp, SWT.RIGHT);
		wlCAFile.setText(Messages.getString("MQTTClientDialog.CAFile.Label")); //$NON-NLS-1$
		props.setLook(wlCAFile);
		FormData fdlCAFile = new FormData();
		fdlCAFile.left = new FormAttachment(0, -margin);
		fdlCAFile.right = new FormAttachment(middle, -2 * margin);
		fdlCAFile.top = new FormAttachment(0, 2 * margin);
		wlCAFile.setLayoutData(fdlCAFile);

		wCAFile = new TextVar(transMeta, wSSLComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		wCAFile.setToolTipText(Messages
				.getString("MQTTClientDialog.CAFile.Tooltip"));
		props.setLook(wCAFile);
		wCAFile.addModifyListener(lsMod);
		FormData fdCAFile = new FormData();
		fdCAFile.left = new FormAttachment(middle, -margin);
		fdCAFile.top = new FormAttachment(0, 2 * margin);
		fdCAFile.right = new FormAttachment(100, 0);
		wCAFile.setLayoutData(fdCAFile);
		lastControl = wCAFile;

		// Client certificate file path
		Label wlCertFile = new Label(wSSLComp, SWT.RIGHT);
		wlCertFile.setText(Messages
				.getString("MQTTClientDialog.CertFile.Label")); //$NON-NLS-1$
		props.setLook(wlCertFile);
		FormData fdlCertFile = new FormData();
		fdlCertFile.left = new FormAttachment(0, -margin);
		fdlCertFile.right = new FormAttachment(middle, -2 * margin);
		fdlCertFile.top = new FormAttachment(lastControl, margin);
		wlCertFile.setLayoutData(fdlCertFile);

		wCertFile = new TextVar(transMeta, wSSLComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		wCertFile.setToolTipText(Messages
				.getString("MQTTClientDialog.CertFile.Tooltip"));
		props.setLook(wCertFile);
		wCertFile.addModifyListener(lsMod);
		FormData fdCertFile = new FormData();
		fdCertFile.left = new FormAttachment(middle, -margin);
		fdCertFile.top = new FormAttachment(lastControl, margin);
		fdCertFile.right = new FormAttachment(100, 0);
		wCertFile.setLayoutData(fdCertFile);
		lastControl = wCertFile;

		// Client key file path
		Label wlKeyFile = new Label(wSSLComp, SWT.RIGHT);
		wlKeyFile.setText(Messages.getString("MQTTClientDialog.KeyFile.Label")); //$NON-NLS-1$
		props.setLook(wlKeyFile);
		FormData fdlKeyFile = new FormData();
		fdlKeyFile.left = new FormAttachment(0, -margin);
		fdlKeyFile.right = new FormAttachment(middle, -2 * margin);
		fdlKeyFile.top = new FormAttachment(lastControl, margin);
		wlKeyFile.setLayoutData(fdlKeyFile);

		wKeyFile = new TextVar(transMeta, wSSLComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		wKeyFile.setToolTipText(Messages
				.getString("MQTTClientDialog.KeyFile.Tooltip"));
		props.setLook(wKeyFile);
		wKeyFile.addModifyListener(lsMod);
		FormData fdKeyFile = new FormData();
		fdKeyFile.left = new FormAttachment(middle, -margin);
		fdKeyFile.top = new FormAttachment(lastControl, margin);
		fdKeyFile.right = new FormAttachment(100, 0);
		wKeyFile.setLayoutData(fdKeyFile);
		lastControl = wKeyFile;

		// Client key file password path
		Label wlKeyPassword = new Label(wSSLComp, SWT.RIGHT);
		wlKeyPassword.setText(Messages
				.getString("MQTTClientDialog.KeyPassword.Label")); //$NON-NLS-1$
		props.setLook(wlKeyPassword);
		FormData fdlKeyPassword = new FormData();
		fdlKeyPassword.left = new FormAttachment(0, -margin);
		fdlKeyPassword.right = new FormAttachment(middle, -2 * margin);
		fdlKeyPassword.top = new FormAttachment(lastControl, margin);
		wlKeyPassword.setLayoutData(fdlKeyPassword);

		wKeyPassword = new TextVar(transMeta, wSSLComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER | SWT.PASSWORD);
		wKeyPassword.setToolTipText(Messages
				.getString("MQTTClientDialog.KeyPassword.Tooltip"));
		props.setLook(wKeyPassword);
		wKeyPassword.addModifyListener(lsMod);
		FormData fdKeyPassword = new FormData();
		fdKeyPassword.left = new FormAttachment(middle, -margin);
		fdKeyPassword.top = new FormAttachment(lastControl, margin);
		fdKeyPassword.right = new FormAttachment(100, 0);
		wKeyPassword.setLayoutData(fdKeyPassword);
		lastControl = wKeyPassword;

		FormData fdSSLComp = new FormData();
		fdSSLComp.left = new FormAttachment(0, 0);
		fdSSLComp.top = new FormAttachment(0, 0);
		fdSSLComp.right = new FormAttachment(100, 0);
		fdSSLComp.bottom = new FormAttachment(100, 0);
		wSSLComp.setLayoutData(fdSSLComp);

		wSSLComp.layout();
		wSSLTab.setControl(wSSLComp);

		// ====================
		// BUTTONS
		// ====================
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// ====================
		// END OF TAB FOLDER
		// ====================
		FormData fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.top = new FormAttachment(wInputField, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom = new FormAttachment(wOK, -margin);
		wTabFolder.setLayoutData(fdTabFolder);

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

		wTabFolder.setSelection(0);

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

		wRequiresAuth.setSelection(producerMeta.isRequiresAuth());
		wRequiresAuth.notifyListeners(SWT.Selection, new Event());

		wUsername.setText(Const.NVL(producerMeta.getUsername(), ""));
		wPassword.setText(Const.NVL(producerMeta.getPassword(), ""));

		wCAFile.setText(Const.NVL(producerMeta.getSSLCaFile(), ""));
		wCertFile.setText(Const.NVL(producerMeta.getSSLCertFile(), ""));
		wKeyFile.setText(Const.NVL(producerMeta.getSSLKeyFile(), ""));
		wKeyPassword.setText(Const.NVL(producerMeta.getSSLKeyFilePass(), ""));

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

		boolean requiresAuth = wRequiresAuth.getSelection();
		producerMeta.setRequiresAuth(requiresAuth);
		if (requiresAuth) {
			producerMeta.setUsername(wUsername.getText());
			producerMeta.setPassword(wPassword.getText());
		}

		producerMeta.setSSLCaFile(wCAFile.getText());
		producerMeta.setSSLCertFile(wCertFile.getText());
		producerMeta.setSSLKeyFile(wKeyFile.getText());
		producerMeta.setSSLKeyFilePass(wKeyPassword.getText());

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
