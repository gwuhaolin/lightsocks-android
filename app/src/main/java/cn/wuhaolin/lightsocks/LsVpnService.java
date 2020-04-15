package cn.wuhaolin.lightsocks;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

import tun.Tun;

public class LsVpnService extends VpnService {
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    private static final int VPN_MTU = 1500;
    private static final String PRIVATE_VLAN4_CLIENT = "172.19.0.1";
    private static final String PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1";
    private ParcelFileDescriptor fileDescriptor;
    private Thread lsThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            switch (intent.getAction()) {
                case ACTION_START:
                    this.startVPN();
                    break;
                case ACTION_STOP:
                    this.stopVPN();
                    break;
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // http://www.voidcn.com/article/p-ksuzgbjk-xp.html
    private void startVPN() throws Exception {
        if (this.fileDescriptor != null) {
            this.stopVPN();
        }
        Builder builder = new Builder();
        builder.setSession(String.valueOf(R.string.app_name));
        // 即表示虚拟网络端口的最大传输单元，如果发送的包长度超过这个数字，则会被分包；一般设为1500
        builder.setMtu(VPN_MTU);
        builder.addAddress(PRIVATE_VLAN4_CLIENT, 30);//设置虚拟主机地址和端口
        builder.addAddress(PRIVATE_VLAN6_CLIENT, 126);
        builder.addRoute("0.0.0.0", 0);//设置允许通过的路由
        builder.addRoute("::", 0);
        builder.addDnsServer("223.5.5.5");
        builder.addDnsServer("2400:3200::1");
        builder.addDnsServer("8.8.8.8");
        builder.addDnsServer("2001:4860:4860::8888");
        builder.addDisallowedApplication(this.getApplication().getPackageName());
        this.fileDescriptor = builder.establish();
        final int fd = this.fileDescriptor.getFd();
        this.lsThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Tun.startTunServer(
                            "IDDpLLSt1w8VzTunC2+ykolrW8eLLQXUCEoJoTnDJlgru+Iv+8767dGZM27Jml9kHnRt2eOMEcpxgT3fyOh5WXuqbMUS4L+57mqg+F61xGKFdd46aVY/ExB9QP2TVJhVnI24sXr/aKTnm+Van9MHGZ66I0PGNydXDaXW6iK+Rd3YkYRgAHyK/MId8uHbow718EKsFKkM8y4+glBPBPazUXMf1UlThtJdAst3TFyUS5Z4cGE82jKd3GaHrqIa7I/k5kFGJER+CqZjgJcxG4NNOPcX0MwBq8Dxt71HBvSONSEp66jvwf5lFhwYKLwqA3J/UvnPiEiVkHavNE42JbawZw==",
                            "dy.wuhaolin.cn:12315",
                            fd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        lsThread.start();
    }

    private void stopVPN() {
        try {
            if (this.fileDescriptor == null) {
                return;
            }
            this.fileDescriptor.close();
            this.fileDescriptor = null;
            this.lsThread.interrupt();
            this.lsThread = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
