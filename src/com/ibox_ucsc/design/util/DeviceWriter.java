package com.ibox_ucsc.design.util;
import java.io.File;
import java.io.FileNotFoundException; import java.io.FileOutputStream; import java.io.IOException;
import java.io.OutputStreamWriter;
public class DeviceWriter { private String path;
private String fileName; private FileOutputStream fos;
private OutputStreamWriter osw;
     /*
      * Constructor with default value;
      */
public DeviceWriter() {
path = new String("/sdcard/"); fileName = new String("samples.txt"); initialize();
}
     /*
      * Constructor with a specified input
      */
public DeviceWriter(String s) { if (s.contains("/")) {
this.path = s.substring(0, s.lastIndexOf("/") + 1);
this.fileName = s.substring(s.lastIndexOf("/") + 1); } else {
this.path = "/sdcard/";
this.fileName = s; }
          initialize();
     }
     /*
      * Write a string to the specified file
      */
public void write(String s) { try {
this.osw.write(s);
this.osw.flush();
} catch (IOException e) { }
}
public void close() { try {
this.osw.flush(); this.osw.close(); this.fos.close();
} catch (IOException e) {
} }
public String toString() {
return new String(this.path + this.fileName);

}
/*
 * Initialize the output stream in order to write
 * to the specified file.
 */
private void initialize() {
File f = new File(new String(this.path + this.fileName)); try {
this.fos = new FileOutputStream(f);
} catch (FileNotFoundException e) {
}
this.osw = new OutputStreamWriter(this.fos);
}
/*
 * ::::::::::::::::::::::::::::::::::::
 *   get & set methods
 * ::::::::::::::::::::::::::::::::::::
 */
public void setPath(String path) { this.path = path;
}
public void setFileName(String fileName) { this.fileName = fileName;
}
public String getPath() { return this.path;
}
public String getFileName() { return this.fileName;
} }

