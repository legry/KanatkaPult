#include <ESP8266WiFi.h>
#include <WebSocketsServer.h>
#include <Ticker.h>

#define clYellow D1 // Главный движок
#define clGreen D2

#define clLime D3 // Быстрый ход
#define claqua D5

#define clFuchsia D6 // Медл ход
#define clblue D7

volatile boolean i20, inic, sok, autmain, hodback, hodfront, gradvalan, strt, alstp, chng, porog, ampok;
volatile int ust = 20;
volatile int amp = 0;
byte btnconf; /*alstp, grdval, autmain, hodfront, hodback*/

IPAddress ip_addr;
uint8_t mynum;
WebSocketsServer webSocket = WebSocketsServer(81);
Ticker ticker;
volatile boolean isConn = false;

void ampers(int ss)
{
    amp = round((ss * 1) / 60);
}

void ampot()
{
    static int i;
    static unsigned int otamp, ss, ss0;
    otamp = analogRead(A0);
    ss = ss + otamp + otamp;
    if (i == 0)
    {
        ss = otamp;
        i = i + 1;
    }
    else
    {
        if (i == 20)
        {
            ss = ss + otamp;
            ampers(ss);
            i20 = true;
            ss = 0;
            i = 0;
        }
        else if ((i > 0) && (i < 20))
        {
            ss = ss + otamp + otamp;
            i = i + 1;
        }
    }
}

void Start()
{
    if (strt)
    {
        if (gradvalan)
        {
            digitalWrite(clYellow, LOW);
        }
        if (!(gradvalan))
        {
            digitalWrite(clGreen, LOW);
        }
        ticker.attach_ms(1, ampot);
        i20 = false;
    }
    else if (!(strt))
    {
        digitalWrite(clYellow, HIGH);
        digitalWrite(clGreen, HIGH);
        ticker.detach();
        amp = 0;
        i20 = true;
    }
}

void hod()
{
    digitalWrite(clblue, HIGH);
    digitalWrite(clFuchsia, HIGH);
    digitalWrite(claqua, HIGH);
    digitalWrite(clLime, HIGH);
    if ((alstp) && !(!(hodback) and !(hodfront)))
    {
        if (autmain)
        {
            if (hodback)
            {
                digitalWrite(clLime, LOW);
            }
            else if (hodfront)
            {
                digitalWrite(claqua, LOW);
            }
        }
        else if (!(autmain))
        {
            if ((strt) && (porog))
            {
                if (hodback)
                {
                    digitalWrite(clFuchsia, LOW);
                }
                else if (hodfront)
                {
                    digitalWrite(clblue, LOW);
                }
            }
            else if (!(strt and porog))
            {
                digitalWrite(clblue, HIGH);
                digitalWrite(clFuchsia, HIGH);
            }
        }
    }
    else if (!((alstp) && !(!(hodback) and !(hodfront))))
    {
        digitalWrite(claqua, HIGH);
        digitalWrite(clLime, HIGH);
    }
}

void parser(String str)
{
    if ((str == "<aut>") || (str == "<main>"))
    {
        if (str == "<aut>")
        {
            autmain = false;
        }
        else if (str == "<main>")
        {
            autmain = true;
        };
    };
    if ((str == "<alstp1>") || (str == "<alstp0>"))
    {
        strt = false;
        if (str == "<alstp1>")
        {
            alstp = true;
        }
        else if (str == "<alstp0>")
        {
            alstp = false;
        }
    };
    if ((str == "<back>") || (str == "<not>") || (str == "<front>"))
    {
        if (str == "<back>")
        {
            hodback = true;
            hodfront = false;
        }
        else if (str == "<not>")
        {
            hodback = false;
            hodfront = false;
        }
        else if (str == "<front>")
        {
            hodfront = true;
            hodback = false;
        };
    };
    if ((str == "<left>") || (str == "<right>"))
    {
        strt = false;
        if (str == "<left>")
        {
            gradvalan = false;
        }
        else if (str == "<right>")
        {
            gradvalan = true;
        }
    };
    if ((str == "<start>") && (alstp))
    {
        strt = true;
    };
    if (String(str).substring(0, 4) == String("<ust"))
    {
        ust = (String(str).substring(4)).toInt();
    };
    if (str == "<amp>")
    {
        ampok = true;
    }
    if (str == "<inic>")
    {
        inic = true;
    }
    if (str == "<?ust>")
    {
        webSocket.sendTXT(mynum, "<ust" + String(ust) + ">");
    }
    chng = true;
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t *payload, size_t length)
{
    switch (type)
    {
    case WStype_DISCONNECTED:
        isConn = false;
        break;
    case WStype_CONNECTED:
        isConn = true;
        mynum = num;
        break;
    case WStype_TEXT:
        String _payload = String((char *)&payload[0]);
        parser(_payload);
        break;
    }
}

void setup()
{
    // put your setup code here, to run once:
    alstp = true;
    autmain = true;
    amp = 0;
    ampok = false;
    i20 = false;
    inic = false;
    pinMode(D1, OUTPUT);
    digitalWrite(D1, HIGH);
    pinMode(D2, OUTPUT);
    digitalWrite(D2, HIGH);
    pinMode(D3, OUTPUT);
    digitalWrite(D3, HIGH);
    pinMode(D5, OUTPUT);
    digitalWrite(D5, HIGH);
    pinMode(D6, OUTPUT);
    digitalWrite(D6, HIGH);
    pinMode(D7, OUTPUT);
    digitalWrite(D7, HIGH);
    delay(500);
    WiFi.mode(WIFI_AP);
    WiFi.softAP("Kanatka_2", "aksjdhfg");
    webSocket.begin();
    webSocket.onEvent(webSocketEvent);
}

void loop()
{
    webSocket.loop();
    if (inic)
    {
        String json = "[";
        if (gradvalan)
        {
            json = json + "true,";
        }
        else
            json = json + "false,";
        if (strt)
        {
            json = json + "true,";
        }
        else
            json = json + "false,";
        if (autmain)
        {
            json = json + "true,";
        }
        else
            json = json + "false,";
        if (alstp)
        {
            json = json + "true,";
        }
        else
            json = json + "false,";
        if (hodback)
        {
            json = json + "true,";
        }
        else
            json = json + "false,";
        if (hodfront)
        {
            json = json + "true]";
        }
        else
            json = json + "false]";
        webSocket.sendTXT(mynum, json);
        inic = false;
    };
    if (i20)
    {
        if (ampok)
        {
            webSocket.sendTXT(mynum, "<" + String(amp) + ">");
            ampok = false;
        }
        if (strt) i20 = false;
    };
    if ((porog == false) && (amp < (ust - 1)))
    {
        porog = true;
        chng = true;
    };
    if ((porog == true) && (amp > (ust)))
    {
        porog = false;
        chng = true;
    };
    if (chng)
    {
        Start();
        hod();
        chng = false;
    };
}
