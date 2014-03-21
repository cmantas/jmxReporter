/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.cslab;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.json.*;

public class Metric_test {
    static int port = 7199;
    static String metricsFile="metrics.json";
    static String hostsFile="hosts";
    static boolean verbose=true;

    static int interval=5; //sec
    static List<Metric> metrics=new java.util.LinkedList();
     static List<String> hosts=new java.util.LinkedList();
     
    static Map<String,JMXConnector> hostConnections = new java.util.HashMap(20);
   static boolean list=false;
 
   
    
    static class Metric{
        ObjectName mbeanName;
        String attribute;
        String label;
        String units;

        public Metric(ObjectName inmbeanName, String inattribute, String inlabel, String inunits) {
            mbeanName = inmbeanName;
            attribute = inattribute;
            label = inlabel;
            units = inunits;
        }
        
    }

    public static void main(String[] args) throws InterruptedException {

        readArgs(Arrays.asList(args));
        loadHostFile();
        JSONArray inJson=file2Json(metricsFile);
        loadJsonMetrics(inJson);
        if (hostsFile != null) {
            //TODO: read hosts file
        }
        
        connectToHosts();
        
        //if the list arg was given, only list the available names and exit
        if (list) {
            list();
            System.exit(0);
        }
        
        for(;;){
            reportAll();
            Thread.sleep(interval*1000);
        }
        
 

    }//main
 
    static boolean connectHost(String hostName){
        try {
                String url = "service:jmx:rmi:///jndi/rmi://" + hostName + ":" + port + "/jmxrmi";
                System.out.println("Using host: "+hostName+" ("+url+")");
                JMXServiceURL serviceUrl = new JMXServiceURL(url);
                JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
                hostConnections.put(hostName, jmxc);
            } catch (MalformedURLException ex) {
                System.err.println("ERROR: malformed url");
                return false;
            } catch (IOException ex) {
                System.err.println("ERROR: could not connect to: "+hostName);
                hostConnections.remove(hostName);
                return false;
            }
        return true;
    }
    
    static void connectToHosts(){
        //printing the available host urls, connect to them and keeep the connections
        for (String hostName : hosts) {
            connectHost(hostName);
           }
    }
    
    static public void addMetric(String name, String attribute, String label, String units){
            Metric m = null;
            try {
                ObjectName OName=new ObjectName(name);
                m = new Metric(OName, attribute, label, units);
            } catch (MalformedObjectNameException ex) {
                System.err.println("ERROR: Malformed MBean name: "+name);
                System.err.println(ex.getMessage());
            }
            if(m!=null)
                metrics.add(m);
    }
    
    static void reportMetric(Metric ms, String host) {
        try {
            //construct the service URL
            JMXConnector jmxc = hostConnections.get(host);
            if(jmxc==null){
                System.out.println("Host "+host+" unavailable, attempting to connect");
                if(!connectHost(host)) return;
            }
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            Object rv = mbsc.getAttribute(ms.mbeanName, ms.attribute);
            if(verbose)
                System.out.println(host+ ": "+ms.label + "=" + rv + " " + ms.units);
        }  catch (IOException ex) {
            System.err.println("ERROR: problem in connection with"+host);
        }  catch (MBeanException | ReflectionException ex) {} 
        catch (AttributeNotFoundException ex) {
            System.err.println("ERROR: could not find attribute: "+ms.attribute+ "in host: "+host);
        } catch (InstanceNotFoundException ex) {
            System.err.println("ERROR: instance was not found");
        }
    }
    
    static public JSONArray file2Json(String filename){
        JSONArray ja=null;
        try {
            //read all bytes of a file (NIO)
            byte [] encoded = Files.readAllBytes(Paths.get(filename));
            //decode all bytes to  the default charset and return
            String in = Charset.defaultCharset().
                    decode(ByteBuffer.wrap(encoded)).
                    toString();
            ja = new JSONArray(in);        
        } catch (IOException ex) {
            System.err.println("ERROR: could not read file: "+filename);
        } catch (JSONException ex) {
            System.err.println("ERROR: malformed json input for file:"+filename);
        }
        
        return ja;
        
    }
    
    static public boolean loadHostFile() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(hostsFile), Charset.forName("UTF-8"));
            for (String line : lines) hosts.add(line.trim());
        } catch (IOException ex) {return false;} 
        return true;
    }
    
    static void loadJsonMetrics(JSONArray ja) {
        for (int i = 0; i < ja.length(); i++) {
            //read json args
            String name = "unknown";
            try {
                JSONObject jo = ja.getJSONObject(i);
                name = jo.getString("name");
                String attribute = jo.getString("attribute");
                String label = jo.getString("label");
                String unit = jo.getString("unit");
                Metric m = new Metric(new ObjectName(name), attribute, label, unit);
                metrics.add(m);
            } catch (JSONException ex) {
                System.err.println("ERROR: missing required json attributes for name: " + name);
            } catch (MalformedObjectNameException ex) {
                System.err.println("ERROR: Malformed Object name: " + name);
            }
        }
    }
    
    static public void list() {
        String host = hosts.remove(0);
        try {

            String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
            System.out.println("RMI URL:\t" + url+"");
            JMXServiceURL serviceUrl = new JMXServiceURL(url);
            try (JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null)) {
                MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
                
                System.out.println("List of available names");
                
                Set<ObjectName> names;
                names = new TreeSet<>(mbsc.queryNames(null, null));
                System.out.println("Available names:");
                for (ObjectName name : names) {
                    System.out.println("\tObjectName = " + name);
                }
            }
        } catch (IOException ex) {
            System.err.println("ERROR: failed to query the server " + host);
        }

    }
    //report all metrics from all hosts
    static void reportAll() {
        for (String host : hosts) {
            //report all metrics
            for (Metric m : metrics) {
                reportMetric(m, host);
            }
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
                hosts.add(value);
                break;
            case "verbose":
                if(value.equals("false"))
                    verbose=false;
                break;
            case "interval":
                interval=Integer.parseInt(value);
                System.out.println("Set interval to "+interval);
                break;
            default:
                System.err.println("ERROR: argument "+name+" not recognized");
                
        }
        
    }
}
