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

    // 点击按钮发送消息
    connect(ui->pushButton, &QPushButton::clicked, [this] {
        this->wsClient->sendMessage("ECHO", this->ui->lineEdit->text());
    });

    // 连接到服务器
    wsClient->connectToServer();
}

MainWidget::~MainWidget() {
    delete ui;
    delete wsClient;
}
