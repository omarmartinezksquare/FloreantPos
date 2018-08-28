/**
 * ************************************************************************
 * * The contents of this file are subject to the MRPL 1.2
 * * (the  "License"),  being   the  Mozilla   Public  License
 * * Version 1.1  with a permitted attribution clause; you may not  use this
 * * file except in compliance with the License. You  may  obtain  a copy of
 * * the License at http://www.floreantpos.org/license.html
 * * Software distributed under the License  is  distributed  on  an "AS IS"
 * * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * * License for the specific  language  governing  rights  and  limitations
 * * under the License.
 * * The Original Code is FLOREANT POS.
 * * The Initial Developer of the Original Code is OROCUBE LLC
 * * All portions are Copyright (C) 2015 OROCUBE LLC
 * * All Rights Reserved.
 * ************************************************************************
 */
package com.floreantpos.ui.views.payment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import com.floreantpos.Messages;
import com.floreantpos.config.CardConfig;
import com.floreantpos.model.PaymentTypeWallet;
import com.floreantpos.swing.DoubleTextField;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.GlassPane;
import com.floreantpos.swing.QwertyKeyPad;
import com.floreantpos.ui.dialog.OkCancelOptionDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.gson.JsonObject;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class DWalletDialog extends OkCancelOptionDialog {
	private FixedLengthTextField tfQRTest;
	private FixedLengthTextField tfTEST;
	//private DoubleTextField tfFaceValue;
	//private QwertyKeyPad qwertyKeyPad;
	
	private JComboBox cbPaymentType;
	private WebcamPanel webcamPanel;
	private Webcam webcam;
	
	private String dueAmountText = "Due amount:";
	private String duePointsText  = "Due points:";
	private String token;
	private String QR;
	JLabel lblStatus;
	JLabel lblWaiting;
	JLabel lblBalance;
	JLabel lblAmount;
	
	private double tenderAmount;
	private int pointsAmount;
	private String amount;
	
	private String getStrTenderAmount() {
		return "$ " + new DecimalFormat(".00").format(tenderAmount) ;
	}

	public DWalletDialog(double tenderAmount) {
		super();
		this.tenderAmount = tenderAmount;
		amount = String.valueOf(tenderAmount);
		pointsAmount = (int) (tenderAmount*Double.valueOf(CardConfig.getDWalletPointConversion()));
				
		token = getToken();
		
		System.out.println(token);

		setTitle("DWallet"); //$NON-NLS-1$
		setTitlePaneText("Digital Wallet");

		JPanel panel = getContentPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[][grow]", "[][][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		
		JLabel lblSelectPaymentType = new JLabel("Payment Type"); //$NON-NLS-1$
		lblSelectPaymentType.setFont(new Font(lblSelectPaymentType.getFont().getFontName(), Font.PLAIN, 16));
		panel.add(lblSelectPaymentType, "cell 0 0,alignx leading"); //$NON-NLS-1$
		
		cbPaymentType = new JComboBox();
		cbPaymentType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLabelsAmount();
			}
		});
		panel.add(cbPaymentType, "cell 1 0 3 1,growx"); //$NON-NLS-1$
		cbPaymentType.setModel(new DefaultComboBoxModel<PaymentTypeWallet>(PaymentTypeWallet.values()));
		

//		JLabel lblTextQR = new JLabel("QR"); //$NON-NLS-1$
//		panel.add(lblTextQR, "cell 0 1,alignx trailing"); //$NON-NLS-1$
//		
//		
//		JPopupMenu menu = new JPopupMenu();
//		Action paste = new DefaultEditorKit.PasteAction();
//        paste.putValue(Action.NAME, "Paste");
//        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
//        menu.add( paste );
//		
//        tfQRTest = new FixedLengthTextField();
//        tfQRTest.setLength(300);
//        tfQRTest.setComponentPopupMenu( menu );
//		panel.add(tfQRTest, "cell 1 1,growx"); //$NON-NLS-1$
		
		lblBalance = new JLabel(dueAmountText); //$NON-NLS-1$
		lblBalance.setFont(new Font(lblBalance.getFont().getFontName(), Font.PLAIN, 16));
		panel.add(lblBalance, "cell 0 2,alignx trailing"); //$NON-NLS-1$
		
		lblAmount = new JLabel(getStrTenderAmount()); //$NON-NLS-1$
		lblAmount.setFont(new Font(lblAmount.getFont().getFontName(), Font.PLAIN, 16));
		panel.add(lblAmount, "cell 1 2 3 1,alignx trailing"); //$NON-NLS-1$
		
		webcam = Webcam.getDefault();
		
//		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.setViewSize(webcam.getViewSizes()[1]);
		
		webcamPanel = new WebcamPanel(webcam);
		panel.add(webcamPanel, "cell 1 3 3 1,growx"); //$NON-NLS-1$
		
		
		lblStatus = new JLabel(""); //$NON-NLS-1$
		panel.add(lblStatus, "cell 1 4,alignx trailing"); //$NON-NLS-1$
		
		ImageIcon imageIcon = new ImageIcon("resources/icons/waiting01.gif");
		lblWaiting = new JLabel(imageIcon); //$NON-NLS-1$
		
		panel.add(lblWaiting, "cell 2 4,alignx trailing"); //$NON-NLS-1$
		lblWaiting.setVisible(false);
//		
		
		GlassPane glassPane = new GlassPane();
		glassPane.setOpacity(0.70f);
		
		ImageIcon imageIcon2 = new ImageIcon("resources/icons/resizedOrangeLoader.gif");
		JLabel glassLabel = new JLabel(imageIcon2);
		glassPane.add(glassLabel);
		this.setGlassPane(glassPane);
//		this.getGlassPane().setVisible(true);
		
		

	}
	
	protected void updateLabelsAmount() {
		// TODO Auto-generated method stub
		if(cbPaymentType.getSelectedItem() == PaymentTypeWallet.BALANCE) {
			lblBalance.setText(dueAmountText);
			amount = String.valueOf(tenderAmount);
			lblAmount.setText(getStrTenderAmount());
			
		}
		else if(cbPaymentType.getSelectedItem() == PaymentTypeWallet.POINTS){
			lblBalance.setText(duePointsText);
			amount = String.valueOf(pointsAmount);
			lblAmount.setText(amount + " pts");
			
		}
//		lblBalance.setText(cbPaymentType.getSelectedItem().toString());
	}

	public String readQR(BufferedImage bufferedImage) {
		String QR = null;
		try {
			LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Result result = new MultiFormatReader().decode(bitmap);
			
			//webcamPanel.pause();
//			for(ResultPoint rp: result.getResultPoints()) {
//				System.out.println(rp);
//			}
			
			
			QR = result.getText();
			//System.out.println("QR reading: " + QR);
			
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			//System.out.println("There is no QR code in the image");
			//e.printStackTrace();
		}
		return QR;
	}
	
	public void switchGlassPane() {
		
		this.getGlassPane().setVisible(!this.getGlassPane().isVisible());
		
	}

	
	
	@Override
	public void doOk() {
		lblStatus.setText("Reading QR...");
		btnOk.setEnabled(false);
		btnCancel.setEnabled(false); 
		lblWaiting.setVisible(true);
		new Thread() {
			public void run() {
				for(int i = 0; i < 50; i++) {
					QR = readQR(webcam.getImage());
					if(QR !=null && QR.matches(".+\\..+\\..+")) {
						webcamPanel.pause();
						break;
					}
					try {
						Thread.sleep(60);
					} catch (InterruptedException e1) {
						//e1.printStackTrace();
					}
				}
				lblWaiting.setVisible(false);
				if(QR !=null) {
					lblStatus.setText("Transaction Request...");
					switchGlassPane();
					
					if(!postTransaction()) {
						lblStatus.setText("Transaction Failed...");
						btnOk.setEnabled(true);
						btnCancel.setEnabled(true);
						
						switchGlassPane();
						webcamPanel.resume();
					}else {
						setCanceled(false);
						dispose();
					}
				}else {
					lblStatus.setText("QR not found, please retry...");
//					webcamPanel.resume();
					btnOk.setEnabled(true);
					btnCancel.setEnabled(true);
				}

			}
			
			
		}.start();
		
		return;
	}
	
	@Override
	public void dispose(){
		webcam.close();
		super.dispose();
	}

	public String getQR() {
		return tfQRTest.getText();
	}
	
	public String getToken() {
		JsonObject json = new JsonObject();
		json.addProperty("email", CardConfig.getDWalletEmail());    
		json.addProperty("password", CardConfig.getDWalletPassword());    
		
		String postUrl = "https://dwallet.survivorsofmars.tk/api/v1/auth/login";
		Pattern tokenPattern = Pattern.compile(".*?\"token\"\\s*:\\s*\"([^\"]+)\".*");
		String token = null;
		try {
			URL url = new URL(postUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.close();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = IOUtils.toString(in, "UTF-8");

            in.close();
            conn.disconnect();
            
	        Matcher matcher = tokenPattern.matcher(response);
	        if (matcher.matches() && matcher.groupCount() > 0) {
	        	token = matcher.group(1);
	        }

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return token;
	}
	
	public boolean postTransaction() {
		String dWalletUser;
		if(QR != null) {
			dWalletUser = QR;
		}
		else{
			dWalletUser = getQR();
		}
		
		
		JsonObject json = new JsonObject();
		json.addProperty("description", "FloreantPOS");    
		json.addProperty("amount", "-"+amount);    
		json.addProperty("type", cbPaymentType.getSelectedItem().toString());    
//		json.addProperty("purchaseDate", LocalDateTime.now().toString() + "-05:00");    
//		json.addProperty("purchaseDate", LocalDateTime.now().toString() + ZonedDateTime.now().getOffset());    
		json.addProperty("purchaseDate", ZonedDateTime.now().toOffsetDateTime().toString());    
		json.addProperty("qr", dWalletUser);    
		
		System.out.println(json.toString());
		
		String postUrl = "https://dwallet.survivorsofmars.tk/api/v1/transaction/qr";
		try {
			URL url = new URL(postUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(40000);
            conn.setRequestProperty("Authorization", "Bearer " + token );
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.close();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = IOUtils.toString(in, "UTF-8");
            
            System.out.println(response);

            in.close();
            conn.disconnect();
            

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}

}
