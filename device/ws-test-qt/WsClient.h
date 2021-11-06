#ifndef WSCLIENT_H
#define WSCLIENT_H

#include <QString>
#include <QObject>

class WsClientPrivate;
class QWebSocket;
class QTimer;

/**
 * @brief Websocket 客户端，实现了自动重连，发送心跳
 */
class WsClient : public QObject {
    Q_OBJECT

public:
    /**
     * @brief 创建 Websocket 客户端
     *
     * @param serverIpPort 服务器的 Ip 和端口，如: 127.0.0.1:9321
     * @param gatewayId    设备网关的 ID，每个设备网关都有唯一的 ID
     * @param gatewayName  设备网关的名字，不需要唯一
     */
    WsClient(const QString &serverIpPort, const QString &gatewayId, const QString &gatewayName);

    ~WsClient();

    /**
     * @brief 连接到服务器
     */
    void connectToServer();

    /**
     * @brief 发送消息到服务器
     *
     * @param type    消息类型，如 METRICS, ECHO 等
     * @param message 消息
     */
    void sendMessage(const QString &type, const QString &message);

signals:
    /**
     * @brief 与服务器连接成功或者连接断开的信号
     *
     * @param yes 连接成功时 yes 为 true, 连接断开时 yes 为 false
     */
    void connected(bool yes);

    /**
     * @brief 收到消息
     *
     * @param message 消息
     */
    void messageReceived(const QString &message);

private:
    WsClientPrivate *d;
};

#endif // WSCLIENT_H
