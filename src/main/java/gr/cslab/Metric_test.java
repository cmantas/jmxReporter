/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.cslab;

import java.util.List;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class Metric_test {

    static MBeanServerConnection mbsc;
    static int interval=5; //sec
    static List<MetricSet> metricSets=new java.util.LinkedList();
    
    class MetricSet{
        ObjectName mbeanName;
        List attributes;
        
        public MetricSet(ObjectName inName, List atts){            
            mbeanName=inName;
            attributes=atts;
        }
        
    }

public static void main(String[] args) throws Exception {
   
String host = "c1";  // or some A.B.C.D
int port = 7199;
int interval = 5; //seconds


String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
System.out.println("RMI URL:\n"+url);
JMXServiceURL serviceUrl = new JMXServiceURL(url);
JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
mbsc =jmxc.getMBeanServerConnection();

//Set<ObjectName> names = 
//    new TreeSet<ObjectName>(mbsc.queryNames(null, null));
//for (ObjectName name : names) {
//    
//    //System.out.println("\tObjectName = " + name);
//    //Object a = mbsc.queryMBeans(name, null);
//    //System.out.println(a);
//
//}

String name = "org.apache.cassandra.metrics:type=ClientRequest,scope=RangeSlice,name=Latency";
ObjectName mbeanName = new ObjectName(name);
    System.out.println(mbeanName.getCanonicalName());
    System.out.println(name);
    System.out.println(mbeanName.getCanonicalName().equals(name));



        //close the connection
        jmxc.close();



    }//main
 
    
    public static void printInfo(){
        
    }
    
    static public void addMetric(String name, String attribute){
            MetricSet ms = null;
            ObjectName OName=null;
            try {
                OName=new ObjectName(name);
            } catch (MalformedObjectNameException ex) {
                System.err.println("ERROR: Malformed MBean name: "+name);
                System.err.println(ex.getMessage());
            }
            for(MetricSet m: metricSets){
                if (m.mbeanName.equals(OName)){
                    ms=m;
                    break;
                }
            }

        
    }
    
    static public void reportAll(MetricSet ms) throws Exception {
        
    for (;;) {
            Object rv = mbsc.getAttribute(ms.mbeanName, "Mean");
            System.out.println(rv);
            rv = mbsc.getAttribute(ms.mbeanName, "LatencyUnit");
            System.out.print(rv);
            Thread.sleep(interval * 1000);
        }
    }
    
}
