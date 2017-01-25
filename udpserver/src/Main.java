import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

import static com.sun.xml.internal.bind.v2.util.ClassLoaderRetriever.getClassLoader;

public class Main {
    /**
     * req�пɰ������²�����
     * day����ǰ�����밲װ����֮�0�ǵ���
     * channel��������
     * os������ϵͳ�汾��
     * safe����ȫ���
     * mc_type: mc�ͻ�������
     * mc_ver: mc�ͻ��˰汾
     * mc_gid: mc��Ϸid
     */
    public static String handleRequest(String req) {
        String result = "{}";
        Properties ps = getLastedPropertys();
        if (ps.getProperty("enable", "false").equals("false")) {
            return result;
        }

        if (_engine instanceof Invocable) {
            Invocable invocable = (Invocable)_engine;
            Map<String, String> params = SplitGetParams(req);

            // JS������÷���
            try {
                Object address ;
                address = invocable.invokeFunction("getAddress",  params.get("mc_type"), params.get("mc_ver"), params.get("mc_gid"), params.get("day"), params.get("channel"),params.get("os"), params.get("safe"));
                System.out.println("The result is: " + address);
                if (address.getClass().equals(String.class)) {
                    result = address.toString();
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        _engine = manager.getEngineByName("js");
        if (_engine == null) {
            System.out.println("ScriptEngine create error!");
        }

        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData;// = new byte[1024];
        while (true) {
            // receive
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            // print received info
            String sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);

            // get remote address
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            // do something to received info
            String result = handleRequest(sentence);
            sendData = result.getBytes();

            // send it back to client
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }

    /**
     * ������url�����еļ�ֵ��
     * �� "Action=del&id=123"��������Action:del,id:123����map��
     *
     * @param strUrlParam String �����б�
     * @return url�����������
     */
    private static Map<String, String> SplitGetParams(String strUrlParam) {
        Map<String, String> mapRequest = new HashMap<>();

        String[] arrSplit;
        //ÿ����ֵΪһ��
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = strSplit.split("[=]");

            //��������ֵ
            if (arrSplitEqual.length > 1) {
                //��ȷ����
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    //ֻ�в���û��ֵ��������
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    /**
     * ��ȡ���µ������ļ�
     *
     * @return ���ö���
     */
    private static Properties getLastedPropertys()  {
        Boolean reload = false;
        if (_lastUpdateDate == null) {
            _lastUpdateDate = new Date();
            reload = true;
        } else {
            Date now = new Date();

            // ����1�������¼���
            if (now.getTime() - _lastUpdateDate.getTime() > 1000 * 60) {
                reload = true;
                _lastUpdateDate = now;
            }
        }

        if (reload) {
            InputStream inputStream = getClassLoader().getResourceAsStream("config.properties");
            Properties p = new Properties();
            try {
                p.load(inputStream);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            Reader scriptReader = new InputStreamReader(
                    getClassLoader().getResourceAsStream("check.js"));

            try {
                // JS��������ļ�
                _engine.eval(scriptReader);
            } catch (ScriptException e) {
                e.printStackTrace();
            } finally {
                try {
                    scriptReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            _config = p;
        }
        return _config;
    }

    static private Date _lastUpdateDate;
    static private Properties _config;
    static private ScriptEngine _engine;
}
