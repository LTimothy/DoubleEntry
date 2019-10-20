import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DoubleEntry extends JFrame
{
	private Label filenameLabel;    // Declare a Label component 
	private TextField filenameText; // Declare a TextField component 
	private Button filenameBtn;   // Declare a Button component
	private String filename;

	public DoubleEntry() {
		setLayout(new FlowLayout());

		// Filename Loading
		filenameLabel = new Label("Enter Filename");
		add(filenameLabel);

		filenameText = new TextField(20);
		filenameText.setEditable(true);
		add(filenameText);

		filenameBtn = new Button("Submit");
		filenameBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				filename = filenameText.getText();
				System.out.println(filename);
			}
		});
		add(filenameBtn);

		setTitle("Double Entry");
		//setSize(250, 100);
		setBounds(100, 100, 400, 400);

		setVisible(true);
	}

	public static void main (String args[]) {
		DoubleEntry instance = new DoubleEntry();
		try {
			// new QualtricsDE();
		} catch (Exception e) {
			System.out.println("Failed to run QualtricsDE.");
		}
	}
}