#include "WsClient.h"

#include <QWebSocket>
#include <QTimer>
#include <QDebug>

/*-----------------------------------------------------------------------------|
 |                         WsClientPrivate implementation                      |
 |----------------------------------------------------------------------------*/
class WsClientPrivate {
public:
    WsClientPrivate(const QString &serverIpPort, const QString &gatewayId, const QString &gatewayName);
    ~WsClientPrivate();

    // 连接到服务器的 Websocket 连接字符串，例如 ws://127.0.0.1:9321?gatewayId=1&gatewayName=bob
    QString connectUrl() const;

    QString serverIpPort;   // 服务器的 IP Port
    QString gatewayId;      // 设备网关 Id
    QString gatewayName;    // 设备网关名字
    QWebSocket *socket;     // Websocket 对象
    QTimer *heartbeatTimer; // 心跳定时器
    bool connected;         // 是否已经和服务器连接上
};

WsClientPrivate::WsClientPrivate(const QString &serverIpPort, const QString &gatewayId, const QString &gatewayName) {
    this->serverIpPort   = serverIpPort;
    this->gatewayId      = gatewayId;
    this->gatewayName    = gatewayName;
    this->connected      = false;
    this->socket         = new QWebSocket();
    this->heartbeatTimer = new QTimer();

    // 启动心跳定时器，定时给服务器发送心跳消息
    this->heartbeatTimer->start(10000);
}

WsClientPrivate::~WsClientPrivate() {
    heartbeatTimer->stop();
    heartbeatTimer->deleteLater();
    socket->close();

    delete heartbeatTimer;
    delete socket;
}

QString WsClientPrivate::connectUrl() const {
    return QString("ws://%1?gatewayId=%2&gatewayName=%3")
                .arg(serverIpPort)
                .arg(gatewayId)
                .arg(gatewayName);
}

/*-----------------------------------------------------------------------------|
 |                            WsClient implementation                          |
 |----------------------------------------------------------------------------*/
WsClient::WsClient(const QString &serverIpPort, const QString &gatewayId, const QString &gatewayName) : QObject() {
    d = new WsClientPrivate(serverIpPort, gatewayId, gatewayName);

    // 连接成功
    QObject::connect(d->socket, &QWebSocket::connected, [this] {
        d->connected = true;
        emit this->isConnected();
    });

    // 连接断开
    QObject::connect(d->socket, &QWebSocket::disconnected, [this] {
        d->connected = false;
        emit this->isDisconnected();
    });

    // 收到消息
    QObject::connect(d->socket, &QWebSocket::textMessageReceived, [this](const QString &message) {
        emit this->messageReceived(message);
    });

    // 定时发送心跳，当服务器端在指定时间内没有收到客户端的心跳消息，服务器会主动断开对应的连接
    QObject::connect(d->heartbeatTimer, &QTimer::timeout, [this] {
        if (d->connected) {
            d->socket->sendTextMessage("{\"type\": \"HEARTBEAT\"}");
        }
    });
}

WsClient::~WsClient() {
    delete d;
}

void WsClient::connectToServer() {
    if (d->connected) {
        return;
    }

    QString url = d->connectUrl();
    d->socket->open(QUrl(url));
    qDebug() << "连接到服务器: " << url;
}

void WsClient::sendMessage(const QString &type, const QString &message) {
    // 消息使用 JSON 格式，如 {"type": "ECHO", "content": "Hello"}
    QString msg = QString("{\"type\": \"%1\", \"content\": \"%2\"}").arg(type).arg(message);

    d->socket->sendTextMessage(msg);
}
