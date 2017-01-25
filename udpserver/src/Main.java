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
     * req中可包含以下参数：
     * day：当前日期与安装日期之差，0是当天
     * channel：渠道号
     * os：操作系统版本号
     * safe：安全软件
     * mc_type: mc客户端类型
     * mc_ver: mc客户端版本
     * mc_gid: mc游戏id
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

            // JS引擎调用方法
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
     * 解析出url参数中的键值对
     * 如 "Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param strUrlParam String 参数列表
     * @return url请求参数部分
     */
    private static Map<String, String> SplitGetParams(String strUrlParam) {
        Map<String, String> mapRequest = new HashMap<>();

        String[] arrSplit;
        //每个键值为一组
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    /**
     * 获取最新的配置文件
     *
     * @return 配置对象
     */
    private static Properties getLastedPropertys()  {
        Boolean reload = false;
        if (_lastUpdateDate == null) {
            _lastUpdateDate = new Date();
            reload = true;
        } else {
            Date now = new Date();

            // 大于1分钟重新加载
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
                // JS引擎解析文件
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
