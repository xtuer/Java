#include "MainWidget.h"
#include "ui_MainWidget.h"
#include "Json.h"
#include "WsClient.h"

#include <QDebug>
#include <QTimer>

MainWidget::MainWidget(QWidget *parent) : QWidget(parent), ui(new Ui::MainWidget) {
    ui->setupUi(this);

    // 提示: 创建 Websocket 连接，服务器的 IP 根据实际情况填写，端口为 9321
    wsClient = new WsClient("127.0.0.1:9321", "gw-1", "gw-1");
    wsReconnectTimer = new QTimer();

    connect(wsClient, &WsClient::isConnected, [this] {
        this->ui->stateLabel->setText("连接成功");
    });
    connect(wsClient, &WsClient::isDisconnected, [this] {
        this->ui->stateLabel->setText("连接断开");
    });

    // 收到服务器发来的消息
    connect(wsClient, &WsClient::messageReceived, [this](const QString &message) {
        this->ui->responseLabel->setText(message);
    });

    // 连接断开后定时尝试重连
    connect(wsReconnectTimer, &QTimer::timeout, [this] {
        wsClient->connectToServer(); // 如果已经是连接成功状态，不会重复连接
    });

    // 点击按钮发送消息
    connect(ui->pushButton, &QPushButton::clicked, [this] {
        this->wsClient->sendMessage("ECHO", this->ui->lineEdit->text());
    });

    // 注意: 非常重要
    // 定时 5 秒尝试连接到服务器，如果连接断开了，测试方法: 连接上服务器，然后把服务器关闭，再打开服务器，查看连接状态。
    // 如果不这么做，服务器重启后，设备网关连接断开不主动自动重连，那么只有重启设备网关的程序才会再次连接。
    wsReconnectTimer->start(5000);

    // 连接到服务器
    wsClient->connectToServer();
}

MainWidget::~MainWidget() {
    delete ui;
    delete wsClient;
}
