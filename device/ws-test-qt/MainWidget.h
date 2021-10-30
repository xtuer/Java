#ifndef MAINWIDGET_H
#define MAINWIDGET_H

#include <QWidget>
#include <QAtomicInt>

class WsClient;
class QTimer;

namespace Ui {
class MainWidget;
}

class MainWidget : public QWidget {
    Q_OBJECT

public:
    explicit MainWidget(QWidget *parent = nullptr);
    ~MainWidget();

private:
    Ui::MainWidget *ui;
    WsClient *wsClient;
    QTimer *wsReconnectTimer; // 如果连接断开，定时重连
};

#endif // MAINWIDGET_H
