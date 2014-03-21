/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.cslab;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
<<<<<<< HEAD:src/metric_test/Metric_test.java
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
=======
>>>>>>> e7291b94fb26c48db2f6b09adc51a1a5149dfb2b:src/main/java/gr/cslab/Metric_test.java
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class Metric_test {
    static int port = 7199;
    static String metricsFile="dummy.json";
    static String hostsFile;

    static MBeanServerConnection mbsc;
    static int interval=5; //sec
    static List<Metric> metrics=new java.util.LinkedList();
     static List<String> hosts=new java.util.LinkedList();
   static boolean list=false;
 
   
    
    static class Metric{
        ObjectName mbeanName;
        String attribute;
        String label;
        String units;

        public Metric(ObjectName inmbeanName, String inattribute, String inlabel, String inunits) {
            mbeanName = inmbeanName;
            attribute = inattribute;
            inlabel = label;
            units = inunits;
        }
        
    }

public static void main(String[] args) throws Exception {
   
String host = "c1";  // or some A.B.C.D


readArgs(Arrays.asList(args));
file2Json(metricsFile);
if(hostsFile!=null){
    //read hosts file
}

if(list){
    list();
    System.exit(0);
}

String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
System.out.println("RMI URL:\n"+url);
JMXServiceURL serviceUrl = new JMXServiceURL(url);
JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
mbsc =jmxc.getMBeanServerConnection();



String name = "org.apache.cassandra.metrics:type=ClientRequest,scope=RangeSlice,name=Latency";

addMetric(name,"Mean", "read latency", "ms");

ObjectName mbeanName = new ObjectName(name);
    System.out.println(mbeanName.getCanonicalName());
    System.out.println(name);
    System.out.println(mbeanName.getCanonicalName().equals(name));



        //close the connection
        jmxc.close();



    }//main
 
    
    public static void printInfo(){
        
    }
    
    static public void addMetric(String name, String attribute, String label, String units){
            Metric m = null;
            ObjectName OName=null;
            try {
                OName=new ObjectName(name);
                m = new Metric(OName,  attribute, label, units);
            } catch (MalformedObjectNameException ex) {
                System.err.println("ERROR: Malformed MBean name: "+name);
                System.err.println(ex.getMessage());
            }
            if(m!=null)
                metrics.add(m);
            

        
    }
    
    static public void reportAll(Metric ms) throws Exception {
        
    for (;;) {
            Object rv = mbsc.getAttribute(ms.mbeanName, "Mean");
            System.out.println(rv);
            rv = mbsc.getAttribute(ms.mbeanName, "LatencyUnit");
            System.out.print(rv);
            Thread.sleep(interval * 1000);
        }
    }
    
    static public void file2Json(String filename){
        ;
        try {
            //read all bytes of a file (NIO)
            byte [] encoded = Files.readAllBytes(Paths.get(filename));
            //decode all bytes to  the default charset and return
            String in = Charset.defaultCharset().
                    decode(ByteBuffer.wrap(encoded)).
                    toString();
            System.out.println(in);
        } catch (Exception ex) {
            Logger.getLogger(Metric_test.class.getName()).log(Level.SEVERE, 
                    "could not read file: "+filename);
        }
    }
    
    static public void list() {
        String host = hosts.remove(0);
      try {
          
        String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
        System.out.println("RMI URL:\n" + url);
        JMXServiceURL serviceUrl = new JMXServiceURL(url);
        JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
        mbsc = jmxc.getMBeanServerConnection();
        
        System.out.println("List of available names");
        
            Set<ObjectName> names
                    = new TreeSet<ObjectName>(mbsc.queryNames(null, null));
            System.out.println("Available names:");            
            for (ObjectName name : names) {
                System.out.println("\tObjectName = " + name);
            }
        } catch (Exception ex) {
            System.err.println("ERROR: failed to query the server "+host);
        }
    }
    
    static void readArgs(List<String> args){
        for (String a: args){
            int i = a.indexOf("=");
            if (i==-1){
                handleArg(a, null);
                continue;
            }
            String name = a.substring(0, i);
            String value = a.substring(i+1);
            handleArg(name, value);
           
        }
    }
    
    static void handleArg(String name, String value){
        switch(name.toLowerCase()){
            case "port":
                System.out.println("Using port:" + port);
                port=Integer.parseInt(value);
                break;
            case "list":
                list=true;
                break;
            case "host":
                System.out.println("Using host:"+value);
                hosts.add(value);
                
        }
        
    }
}
