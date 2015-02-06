import java.awt.*;
import java.awt.event.*;
import java.io.*;
//testubg
import javax.imageio.IIOException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import java.util.*;
//
public class ConwayOptionGUI extends JDialog implements ActionListener,
  ChangeListener, ItemListener {
  ConwayGame _game;

 public int targetFPS = 60;
 public int targetGPS = 60;
 public int targetThreadCount = 1;
 public int fpsTemp = 60;
 public int gpsTemp = 60;
 public int threadTemp = Runtime.getRuntime().availableProcessors();
 public int error = 0;
 Set<Integer> birthList = new HashSet<Integer>();
 Set<Integer> surviveList = new HashSet<Integer>();
 Set<Integer> birthListTemp = new HashSet<Integer>();
 Set<Integer> surviveListTemp = new HashSet<Integer>();
 static final int FPS_MIN = 20;
 static final int FPS_MAX = 144;
 static final int FPS_INIT = 60; // initial frames per second
 static final int GPS_INIT = 60;
 static final int THREAD_MINIMUM = 0;
 static final int THREAD_MAX = 255;
 static final int THREAD_INIT = 1;
 static final String OPTIONS_FILE = "options.init";
 boolean frozen = false;

 JButton testButton;
 JButton cancelButton;
 JButton confirmButton;

 JPanel fpsAndGpsPanel;
 JPanel threadPanel;
 //JTextField setThreadButton;
 //JButton singleThreadButton,  setThreadButton, multiThreadButton;
 JRadioButton singleThreadOption, multiThreadOption;
 ButtonGroup threadGroup;
 JTextField multiThreadCount;
 JTextField gpsTextField;
 JSlider framesPerSecond;
 PlainDocument numbersOnly;

 JPanel ruleSetPanel;
 JPanel rulesetButtons, threadAndRulesetPanel;
 //JButton normalButton, highlifeButton, customButton;
 JRadioButton normalButton, highlifeButton, customButton;
 ButtonGroup rulesetGroup;
 JCheckBox birthCheckBox[] = new JCheckBox[9];
 JCheckBox surviveCheckBox[] = new JCheckBox[9];
 JPanel confirmPanel, sbPanel;

 JFileChooser fc = new JFileChooser();
 JTextArea log = new JTextArea(5, 20);
 JTextArea statusBar = new JTextArea(5, 20);
/*
 public static void main(String args[]) {

  final ConwayOptionGUI cog = new ConwayOptionGUI();

 }
*/
 public ConwayOptionGUI( ConwayGame game ) {
   _game = game;
  setSize(450, 600);
  setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
  setLayout(new BorderLayout(5, 5));
  //setVisible(true);
  setTitle("Options");
  setResizable(false);
  pack();
  create();
  initialize();
  setLocationRelativeTo(null);
 }

 public void create(){
  log.setMargin(new Insets(5, 5, 5, 5));
  fpsAndGpsPanel = new JPanel();
  fpsAndGpsPanel.setLayout(new BoxLayout(fpsAndGpsPanel,
    BoxLayout.PAGE_AXIS));

  JLabel sliderLabel = new JLabel("Frames Per Second", JLabel.CENTER);
  sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

  framesPerSecond = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX,
    FPS_INIT);

  framesPerSecond.addChangeListener(this);
  framesPerSecond.setMajorTickSpacing(10);
  framesPerSecond.setMinorTickSpacing(1);
  framesPerSecond.setPaintTicks(true);
  framesPerSecond.setPaintLabels(true);
  framesPerSecond.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
  Font font = new Font("Serif", Font.ITALIC, 15);
  framesPerSecond.setFont(font);

  fpsAndGpsPanel.setBorder(BorderFactory
    .createEmptyBorder(10, 10, 10, 10));
  gpsTextField = new JTextField("" + FPS_INIT, 10);
  gpsTextField.setDocument(numberHandler());
  //gpsTextField.setActionCommand("gpsTextField");
  //gpsTextField.addActionListener(this);
  JLabel GPSText = new JLabel("Generations per second");
  JPanel GPSPanel = new JPanel();
  GPSPanel.add(gpsTextField, BorderLayout.EAST);
  GPSPanel.add(GPSText, BorderLayout.WEST);
  fpsAndGpsPanel.add(sliderLabel);
  fpsAndGpsPanel.add(framesPerSecond);
  fpsAndGpsPanel.add(GPSPanel);
  // ////////////////////////////////////////// FPS JSlideBar + GPS
  // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////// threadPanel Panel (3 jbuttons)
  threadPanel = new JPanel();
  singleThreadOption = new JRadioButton("Single Thread");
  singleThreadOption.setToolTipText("The game loop will use a single thread of execution to process each generation.");
  singleThreadOption.setActionCommand("Single");
  singleThreadOption.addActionListener(this);
  multiThreadOption = new JRadioButton("Multithreaded");
  multiThreadOption.setToolTipText("The game loop will use the specified quantity threads of execution to process each generation.");
  multiThreadOption.setActionCommand("Multi");
  multiThreadOption.addActionListener(this);
  multiThreadCount = new JTextField("1", 3);
  multiThreadCount.setDocument(numberHandler());
  multiThreadCount.setToolTipText("The default quantity of threads is based on the number of 'logical' processors reported by your machine to the JVM, and in most cases is the optimal number.");
  threadGroup = new ButtonGroup();
  threadGroup.add(singleThreadOption);
  threadGroup.add(multiThreadOption);
  
  threadPanel.setLayout(new BorderLayout());
  threadPanel.add(singleThreadOption, BorderLayout.NORTH);
  threadPanel.add(multiThreadOption, BorderLayout.CENTER);
  threadPanel.add(multiThreadCount, BorderLayout.SOUTH);
  
  /*
  singleThreadButton = new JButton("Single-Thread Mode");
  singleThreadButton.setVerticalTextPosition(AbstractButton.CENTER);
  singleThreadButton.setHorizontalTextPosition(AbstractButton.LEADING);
  singleThreadButton.setMnemonic(KeyEvent.VK_D);
  singleThreadButton.setActionCommand("Single");

  setThreadButton = new JButton("Click Here to Override Thread");
  setThreadButton.setVerticalTextPosition(AbstractButton.BOTTOM);
  setThreadButton.setHorizontalTextPosition(AbstractButton.CENTER);
  setThreadButton.setMnemonic(KeyEvent.VK_M);
  setThreadButton.setActionCommand("Set-targetThreadCount");

  multiThreadButton = new JButton("Multi-Threaded Mode");
  multiThreadButton.setMnemonic(KeyEvent.VK_E);
  multiThreadButton.setActionCommand("enable");
  multiThreadButton.setEnabled(false);
  multiThreadButton.setActionCommand("multiThreadButton");

  singleThreadButton.addActionListener(this);
  setThreadButton.addActionListener(this);
  multiThreadButton.addActionListener(this);

  singleThreadButton
    .setToolTipText("Click this button to use a single thread for execution.");
  setThreadButton
    .setToolTipText("Click this button to set the number of threads.");
  multiThreadButton
    .setToolTipText("Click this button to use multiple threads.");
  threadPanel.setLayout(new BorderLayout());
  
  GridLayout experimentLayout = new GridLayout(0,1);
  threadPanel.setLayout(experimentLayout);
  threadPanel.add(singleThreadButton);
  threadPanel.add(setThreadButton);
  threadPanel.add(multiThreadButton); */
  // //////////////////////////// threadPanel Panel (3 jbuttons)
  // ////////////////////////////////////////////////////////////////////////
  // ///////////////////////////Rule Set Panel (BUttonPanel, survive
  // Panel, birth Panel)

  ruleSetPanel = new JPanel();
  ruleSetPanel.setLayout(new BorderLayout());
  rulesetButtons = new JPanel();
  rulesetGroup = new ButtonGroup();
  
  normalButton = new JRadioButton("Normal");
  highlifeButton = new JRadioButton("HighLife");
  customButton = new JRadioButton("Custom");
  
  normalButton.addActionListener(this);
  highlifeButton.addActionListener(this);
  customButton.addActionListener(this);
  
  normalButton.setActionCommand("NORMAL");
  highlifeButton.setActionCommand("HALFLIFE");
  customButton.setActionCommand("CUSTOM");
  
  normalButton.setToolTipText("The default ruleset of 3B/23S");
  highlifeButton.setToolTipText("The altered ruleset of 36B/23S");
  customButton.setToolTipText("Allows you to specify your own ruleset using the checkboxes");
  
  rulesetGroup.add(normalButton);
  rulesetGroup.add(highlifeButton);
  rulesetGroup.add(customButton);
  /*
  normalButton = new JButton("Normal");
  normalButton.setVerticalTextPosition(AbstractButton.CENTER);
  normalButton.setHorizontalTextPosition(AbstractButton.LEADING);
  normalButton.setMnemonic(KeyEvent.VK_D);
  normalButton.setActionCommand("NORMAL");

  highlifeButton = new JButton("HighLife");
  highlifeButton.setVerticalTextPosition(AbstractButton.BOTTOM);
  highlifeButton.setHorizontalTextPosition(AbstractButton.CENTER);
  highlifeButton.setMnemonic(KeyEvent.VK_M);
  highlifeButton.setActionCommand("HALFLIFE");

  customButton = new JButton("Custom");
  customButton.setVerticalTextPosition(AbstractButton.BOTTOM);
  customButton.setHorizontalTextPosition(AbstractButton.CENTER);
  customButton.setMnemonic(KeyEvent.VK_E);
  customButton.setActionCommand("CUSTOM");
  */

  rulesetButtons.setLayout(new BorderLayout());
  rulesetButtons.add(normalButton, BorderLayout.NORTH);
  rulesetButtons.add(highlifeButton, BorderLayout.CENTER);
  rulesetButtons.add(customButton, BorderLayout.SOUTH);

  // customPanel = new JPanel();
  sbPanel = new JPanel();
  sbPanel.setLayout(new GridLayout(10, 2, 10, 0));
  sbPanel.add(new JLabel("Survival Rules"));
  sbPanel.add(new JLabel("Birth Rules"));
  for (int i = 0; i < surviveCheckBox.length; i++) {
   surviveCheckBox[i] = new JCheckBox("" + i);
   surviveCheckBox[i].addItemListener(this);
   surviveCheckBox[i].setEnabled(false);
   surviveCheckBox[i].setActionCommand("s" + Integer.toString(i));
   sbPanel.add(surviveCheckBox[i]);

   birthCheckBox[i] = new JCheckBox("" + i);
   birthCheckBox[i].addItemListener(this);
   birthCheckBox[i].setEnabled(false);
   birthCheckBox[i].setActionCommand("b" + Integer.toString(i));
   sbPanel.add(birthCheckBox[i]);
  }
  
  //sbPanel.showBorder(true);
  
  threadAndRulesetPanel = new JPanel();
  threadAndRulesetPanel.setLayout(new BorderLayout());
  threadAndRulesetPanel.add(rulesetButtons, BorderLayout.NORTH);
  threadAndRulesetPanel.add(threadPanel, BorderLayout.SOUTH);

  ruleSetPanel.add(threadAndRulesetPanel, BorderLayout.WEST);
  ruleSetPanel.add(sbPanel, BorderLayout.EAST);
  //ruleSetPanel.add(threadPanel, BorderLayout.NORTH);
  testButton = new JButton("DEBUG");
  cancelButton = new JButton("Cancel");
  confirmButton = new JButton("Accept");
  confirmButton.setToolTipText("Click this button to confirm all your changes");
  JButton defaultButton = new JButton("Reset");
  defaultButton.setToolTipText("Click this button to reset everything to default");
  testButton.addActionListener(new ActionListener() {
   public void actionPerformed(ActionEvent e) {

    test();
    test2();

   }
  });
  cancelButton.addActionListener(new ActionListener() {
   public void actionPerformed(ActionEvent e) {

    hideOptions();

   }
  });
  confirmButton.addActionListener(new ActionListener() {
   public void actionPerformed(ActionEvent e) {
    update();
    //confirmButton.setEnabled(false);
    hideOptions();
   }
  });
  defaultButton.addActionListener(new ActionListener() {
   public void actionPerformed(ActionEvent e) {
    resetToDefault();
    //confirmButton.setEnabled(true);
   }
  });

  confirmPanel = new JPanel();
  confirmPanel.setLayout(new BorderLayout());

  JPanel acrPanel = new JPanel();
  acrPanel.setLayout(new GridLayout(1, 3));
  confirmPanel.add(testButton, BorderLayout.WEST);

  acrPanel.add(confirmButton);
  acrPanel.add(cancelButton);
  acrPanel.add(defaultButton);
  
  confirmPanel.add(acrPanel, BorderLayout.EAST);
  // ///////////////////////////Rule Set Panel (BUttonPanel, survive
  // Panel, birth Panel)
  // /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // //////////////////////////HotKey JtextField
  JTextArea hotKeys = new JTextArea(
    "==================\n        Hot Keys: \n Up: UP\n Down: DOWN\n Left: LEFT\n Right: RIGHT\n Pause/Start: ENTER\n create: LEFT CLICK\n delete: SHIFT\n create in bulk: CTRL\n center: HOME\n hide menu: X\nload patterns: INSERT\nenter coordinates: END\nshow/hide grid: TAB\n==================");
  // //////////////////////////HotKey JtextField
////////////////////////////////////////////////////////////////////////////////////////////////////////
  // /////////////////////////// Adding everything together

  add(fpsAndGpsPanel, BorderLayout.NORTH);
  //add(threadPanel, BorderLayout.CENTER);
  add(ruleSetPanel, BorderLayout.EAST);
  add(hotKeys, BorderLayout.WEST);
  add(confirmPanel, BorderLayout.SOUTH);
  pack();
 }
// Change 1
 public void showOptions() {
  framesPerSecond.setValue(targetFPS);  
  selectCheckBoxes(birthList, surviveList); 
  gpsTextField.setText("" + targetGPS);
  multiThreadCount.setText(""+targetThreadCount);
  guiRefresh();
  ruleSetCheck();
  
  setVisible(true);
 }

 public void hideOptions() {
  //confirmButton.setEnabled(false);
  setVisible(false);
 }

 public void resetToDefault() {//change 3
  fpsTemp = 60;
  framesPerSecond.setValue(fpsTemp);
  gpsTemp = 60;
  threadTemp = 1;
  birthListTemp.clear();
  birthListTemp.add(3);
  surviveListTemp.clear();
  surviveListTemp.add(2);
  surviveListTemp.add(3);
  
  selectCheckBoxes(birthListTemp, surviveListTemp);
  //normalButton.setSelected(true);
  //singleThreadOption.setSelected(true);
  gpsTextField.setText("60");
  multiThreadCount.setText("" + threadTemp);
  //multiThreadCount.setEnabled(false);
  guiRefresh();
 }
 
 public void initialize(){
  
  File options = new File(System.getProperty("user.dir")
    + System.getProperty("file.separator") + OPTIONS_FILE);
  if (options.exists()){
   readFromFile(options);
  }else
   try { //CHANGE 2   ======> another potential bug for seeding
    resetToDefault();
    //System.out.println(birthListTemp);
    passingSetValue(birthList, birthListTemp);
    passingSetValue(surviveList, surviveListTemp);
    //selectCheckBoxes(birthListTemp, surviveListTemp);
    options.createNewFile();
   } catch (IOException e) {
    e.printStackTrace();
   }
   gpsTextField.setText("" + targetGPS);
  
   selectCheckBoxes(birthList, surviveList);
   guiRefresh();
   
  //confirmButton.setEnabled(false);
 }
 
 public void guiRefresh(){
     
  //selectCheckBoxes(birthList, surviveList);
  
  if(birthList.size() == 1 && surviveList.size() == 2 && birthList.contains(3) && surviveList.contains(3) && surviveList.contains(2)){ normalButton.setSelected(true); disableCustom();}
  else if(birthList.size() == 2 && surviveList.size() == 2 && birthList.contains(3) && birthList.contains(6) && surviveList.contains(3) && surviveList.contains(2)) {highlifeButton.setSelected(true); disableCustom();}
  else {customButton.setSelected(true); enableCustom();}
   
   if(targetThreadCount == 1) {
     singleThreadOption.setSelected(true);
     multiThreadCount.setText("" + targetThreadCount);
     multiThreadCount.setEnabled(false);
   } else {
     multiThreadOption.setSelected(true);
     multiThreadCount.setText("" + targetThreadCount);
     multiThreadCount.setEnabled(true);
   }  
 
 }
 
 public void enableCustom() { // This function enables the Custom Panel
         // (birth & survive checkbox clusters)

  for (int i = 0; i < birthCheckBox.length; i++) {
   birthCheckBox[i].setEnabled(true);
   surviveCheckBox[i].setEnabled(true);
  }
  birthCheckBox[0].setEnabled(false); // The 0 birth one is always
           // disabled
 }

 public void disableCustom() { // This function disables the Custom Panel
         // (birth & survive checkbox clusters)

  for (int i = 0; i < birthCheckBox.length; i++) {
   birthCheckBox[i].setEnabled(false);
   surviveCheckBox[i].setEnabled(false);
  }
 }

 public void writeToFile(File targetFile) {

  try {
   // File targetFile = new File(fileName);
   // if file doesnt exists, then create it
   FileWriter fw = new FileWriter(targetFile.getAbsoluteFile());
   BufferedWriter bw = new BufferedWriter(fw);
   bw.write(targetGPS + "\n" + targetFPS + "\n" + Math.max(1,Math.min(targetThreadCount, 256))
     + "\n");
   System.err.println(targetFile.getName());
   

   for (int ii : birthList) {
    bw.write(ii + " ");
   }
   bw.write("\n");
   for (int ii : surviveList) {
    bw.write(ii + " ");
   }
   bw.close();
  } catch (IOException e) {
  }
 }

 public void readFromFile(File targetFile) {

  try {
   BufferedReader reader = new BufferedReader(new FileReader(
     targetFile));

   int i = fileLineCount(targetFile);
   System.err.println("Here's how many lines in the file: " + i);
// BUG 4
   if(i != 5 && i != 4){System.err.println("File format not recognized"); return;}
   Scanner sc = new Scanner(targetFile);
   ArrayList<String> x = new ArrayList<String>();
   while (sc.hasNextLine()) {

    x.add((sc.nextLine()));
   }
   
   try{
     targetGPS = Integer.parseInt(x.get(0));
   }catch(NumberFormatException e){
     System.err.println("first line a number please");
   } 
   
   try{
     targetFPS = Integer.parseInt(x.get(1));
   }catch(NumberFormatException e){
     System.err.println("second line a number please");
   } 
   
   try{
     targetThreadCount = Integer.parseInt(x.get(2));
   }catch(NumberFormatException e){
     System.err.println("third line a number please");
   } 
   
   String birthListFromFile = x.get(3);
   String[] birthLine = birthListFromFile.split(" ");
   birthList.clear();
   for (int j = 0; j < birthLine.length; j++) {
     
      try{
        int temp = Integer.parseInt(birthLine[j]);
        birthListTemp.add(temp);
        birthList.add(temp);
      }catch(NumberFormatException e){
        System.err.println("fourth line contains invalid char");
      } 
   
    
   }

   String surviveListFromFile = x.get(4);
   String[] surviveLine = surviveListFromFile.split(" ");
   surviveList.clear();
   for (int k = 0; k < surviveLine.length; k++) {
     try{
        int temp = Integer.parseInt(surviveLine[k]);
        surviveListTemp.add(temp);
        surviveList.add(temp);
     }catch(NumberFormatException e){
        System.err.println("fifth line contains invalid char");
     } 
     
   // surviveListTemp.add(Integer.parseInt(surviveLine[k]));
   // surviveList.add(Integer.parseInt(surviveLine[k]));
    
    
   }
   if(fileInputCheck()==false){
     birthList.clear();
     surviveList.clear();
     birthListTemp.clear();
     surviveListTemp.clear();
     birthList.add(2);
     birthListTemp.add(2);
     surviveListTemp.add(2);
     surviveListTemp.add(3);
     surviveList.add(2);
     surviveList.add(3);
   }
  // System.out.println(birthList);
 //  System.out.println(surviveList);
 //  System.out.println("targetGPS" + targetGPS + "|targetFPS"
 //    + targetFPS + "|targetThreadCount" + targetThreadCount);
   sc.close();
   reader.close();
   
  } catch (Exception e) {
   e.printStackTrace();
  }

 }
 
 public boolean validRuleSetOrNot(){
   
   for(int i : birthList){
     if( i <= 0 || i > 8){
       return false;
     }
   }
   for( int j : surviveList){
     if( j < 0 || j > 8){
       return false;
     }
   }
   return true;
 }
 
 public boolean fileInputCheck(){
 
   if( validRuleSetOrNot() == false){
     JOptionPane.showMessageDialog(null, "Your rule Set values are invalid");
     //confirmButton.setEnabled(false);
     return false;
   }   
   else if( threadTemp < 1 || threadTemp > 255){
     JOptionPane.showMessageDialog(null, "please enter a thread # between 0-255");
     //confirmButton.setEnabled(false);
     return false;
   }    
   return true;
 }
 
 
 public void ruleSetCheck(){
//BUG 3 Seeded
  //System.out.println("CHANES"+birthListTemp);
  
  if(birthListTemp.isEmpty()==true || surviveListTemp.isEmpty() == true){
   //confirmButton.setEnabled(false);
  }
  else{
    //confirmButton.setEnabled(true);
  }
 }

 public int fileLineCount(File filename) throws IOException { 
  InputStream is = new BufferedInputStream(new FileInputStream(filename));
  try {
   byte[] c = new byte[1024];
   int count = 0;
   int readChars = 0;
   boolean empty = true;
   while ((readChars = is.read(c)) != -1) {
    empty = false;
    for (int i = 0; i < readChars; ++i) {
     if (c[i] == '\n') {
      ++count;
     }
    }
   }
   return (count == 0 && !empty) ? 1 : count;
  } finally {
   is.close();
  }
 }

 public void uncheckBoxes() {
  for (int i = 0; i < birthCheckBox.length; i++) {
   
//BUG 2  remove actionListener 
   birthCheckBox[i].removeItemListener(this);
   surviveCheckBox[i].removeItemListener(this);
   birthCheckBox[i].setSelected(false);
   surviveCheckBox[i].setSelected(false);
   birthCheckBox[i].addItemListener(this);
   surviveCheckBox[i].addItemListener(this);
  }
 }

 public void selectCheckBoxes(Set<Integer> birth, Set<Integer> survive) {
  uncheckBoxes();  
  
  for(int i: birth){
   birthCheckBox[i].setSelected(true);
  }
  
  for(int i: survive){
   surviveCheckBox[i].setSelected(true);
  }

 }

 public void update() {
  //System.out.print(birthListTemp);
  int value = 1;
  targetFPS = fpsTemp;
  value = 60;
  try {
 value = Integer.parseInt(gpsTextField.getText());
  } catch (NumberFormatException e) {
 e.printStackTrace();
  } finally {
 targetGPS = value;
  }
  //targetGPS = gpsTemp;
  value = 1;
  try {
 value = Math.max(1,Math.min(Integer.parseInt(multiThreadCount.getText()),256));
  } catch (NumberFormatException e) {
 e.printStackTrace();
  } finally {
 targetThreadCount = value;
  }
  framesPerSecond.setValue(targetFPS);
  birthList.clear();
  birthList.addAll(birthListTemp);
//BUG 1
  //surviveList = surviveListTemp;
  surviveList.clear();
  surviveList.addAll(surviveListTemp);
  selectCheckBoxes(birthList, surviveList);
  File options = new File(System.getProperty("user.dir")
    + System.getProperty("file.separator") + OPTIONS_FILE);
   try {
    options.createNewFile();
    writeToFile(options);
   } catch (IOException e) {
    e.printStackTrace();
   }
 }

 public void downdate() {
  int tempFps = fpsTemp;
  int tempGps = gpsTemp;
  int tempThread = threadTemp;
  gpsTextField.setText("" + targetGPS);
  gpsTemp = targetGPS;
  fpsTemp = targetFPS;
  threadTemp = targetThreadCount;
  multiThreadCount.setText("" + targetThreadCount);
  targetFPS = tempFps;
  targetGPS = tempGps;
  targetThreadCount = tempThread;
  framesPerSecond.setValue(targetFPS);
  statusBar.setText(" Your targetThreadCount now is: "
    + targetThreadCount + "\nYour targetFPS now is " + targetFPS
    + "\nYour targetGPS now is" + targetGPS);
 }
 
 public void passingSetValue(Set<Integer> copyTo, Set<Integer> copyFrom){
  copyTo.clear();
  for(int i: copyFrom){
   copyTo.add(i);
  }
  
 }

 public void actionPerformed(ActionEvent e) { // ActionListener
 String command = e.getActionCommand();
  if ("Single".equals(e.getActionCommand())) { // If
              // "Single button is pressed, change the Thread# to 1"

   //setThreadButton.setEnabled(false);
   //singleThreadButton.setEnabled(false);
   //multiThreadButton.setEnabled(true);
   threadTemp = 1;
   multiThreadCount.setText("1");
   multiThreadCount.setEnabled(false);
   //singleThreadButton.setText("Single targetThreadCount enabled");
   //multiThreadButton.setText("Multi-Thread");

  } /*else if ("Set-targetThreadCount".equals(e.getActionCommand())) { 

   String message = JOptionPane.showInputDialog(
     "Please enter your targetThreadCount #", targetThreadCount);
   if ((message != null) && (message.length() > 0)) {
    int FLAG = isNumeric(message);
    int temp;
    if (FLAG == 0) {
     JOptionPane
       .showMessageDialog(null, "please enter a number");
//BUG 6 set enable button to false
     error = 1;
     return;
    } else {
     temp = Integer.parseInt(message);

     if ((temp < THREAD_MINIMUM || temp > THREAD_MAX)
       || ("".equals(temp))) {
      JOptionPane.showMessageDialog(null,
        "Your number is invalid");
      error = 1;
     } else {
      threadTemp = temp;
      JOptionPane.showMessageDialog(null,
        "Your targetThreadCount # is: " + threadTemp);
      multiThreadButton.setText("Your targetThreadCount#: "
        + threadTemp);
      statusBar.setText("Your targetThreadCount#: "
        + threadTemp);
     }
    }
   }
  } */else if ("Multi".equals(e.getActionCommand())) { // If
                  // "multiThreadButton button is pressed, enable the setThread button"
   threadTemp = Runtime.getRuntime().availableProcessors();
   multiThreadCount.setText("" + threadTemp);
   multiThreadCount.setEnabled(true);
  } /*else if ("gpsTextField".equals(e.getActionCommand())) { 
   
   String text = gpsTextField.getText();
   if (isNumeric(text) == 0) {
    JOptionPane.showMessageDialog(null, "please enter a number");
    error = 1;
   } else {
    int targetGPS = Integer.parseInt(text);
    if (targetGPS < 0 || targetGPS > 255) {
     JOptionPane.showMessageDialog(null,
       "please enter a number between 0-255");
     error = 1;
    } else {
     gpsTemp = targetGPS;
     System.out.println(gpsTemp);
    }
   }
  } */else if ("CUSTOM".equals(e.getActionCommand())) {

   //highlifeButton.setEnabled(true);
   //normalButton.setEnabled(true);
   //customButton.setEnabled(false);
   enableCustom();
  } else if ("NORMAL".equals(e.getActionCommand())) {

   //highlifeButton.setEnabled(true);
   //normalButton.setEnabled(false);
   //customButton.setEnabled(true);
   uncheckBoxes();
// BUG 5:  not clear before setSelected
   birthListTemp.clear();
   surviveListTemp.clear();

   birthCheckBox[3].setSelected(true);
   surviveCheckBox[2].setSelected(true);
   surviveCheckBox[3].setSelected(true);
   disableCustom();
  } else if ("HALFLIFE".equals(e.getActionCommand())) {

   //highlifeButton.setEnabled(false);
   //normalButton.setEnabled(true);
   //customButton.setEnabled(true);
   uncheckBoxes();
   birthListTemp.clear();
   surviveListTemp.clear();
   birthCheckBox[3].setSelected(true);
   birthCheckBox[6].setSelected(true);
   surviveCheckBox[2].setSelected(true);
   surviveCheckBox[3].setSelected(true);

   disableCustom();
  } 
//  if( error == 0){
   ruleSetCheck();
 // }
 // error = 0;
  // //////////////////////////////////////////////////////////////////////////////////
 }

 @Override
 public void itemStateChanged(ItemEvent e) { // ItemListener, here for the
            // checkboxes

  Object source = e.getItemSelectable();
  for (int i = 0; i < birthCheckBox.length; i++) {

   if (source == birthCheckBox[i]) {
    if (e.getStateChange() == ItemEvent.DESELECTED) {
     birthListTemp.remove(new Integer(i));
    } else if (e.getStateChange() == ItemEvent.SELECTED) {
     birthListTemp.add(new Integer(i));     
    }
   }
   else if (source == surviveCheckBox[i]) {
    if (e.getStateChange() == ItemEvent.DESELECTED) {
     surviveListTemp.remove(new Integer(i));
    } else if (e.getStateChange() == ItemEvent.SELECTED) {
     surviveListTemp.add(new Integer(i));

    }
   }
   
  }
  ruleSetCheck();   
 }

 @Override
 public void stateChanged(ChangeEvent e) {

  JSlider source = (JSlider) e.getSource();
  if (!source.getValueIsAdjusting()) {
   fpsTemp = (int) source.getValue();
   //System.out.println(fpsTemp);
  }
  ruleSetCheck();
 }

 private int isNumeric(String str) {
  if (str.equals("")) {
   return 0;
  }
  try {
   double d = Double.parseDouble(str);
  } catch (NumberFormatException nfe) {
   return 0;
  }
  return 1;
 }

 public void test() {

  System.out.println("GPS:" + targetGPS + " FPS:" + targetFPS
    + " Thread:" + targetThreadCount);
  
   System.out.println("GPSTEMP:" + gpsTemp + " FPSTEMP:" + fpsTemp
    + " ThreadTEMP:" + threadTemp);
 }
 

 public void test2() {

  System.out.println("Birth:" + birthList);
  System.out.println("Survive:" + surviveList);
  
  System.out.println("BirthTemp:" + birthListTemp);
  System.out.println("SurviveTemp:" + surviveListTemp);

 }
 
 public PlainDocument numberHandler() {
 return new PlainDocument() {
  public void insertString(int offset, String s, AttributeSet a) {
   if(!Character.isISOControl(s.charAt(0))) {
    if(!this.isInteger(s)) return;
   }
   try {
    super.insertString(offset, s, a);
   } catch (Exception e) {}
  }
  
  private boolean isInteger(String s) {
   for(int i = 0; i < s.length(); i++) {
    if(!Character.isDigit(s.charAt(i))) return false;
   }
   return true;
  }
 };
 }
}
