#ifndef NETWORK_H_
#define NETWORK_H_

/* network thread/interface for lwip. */
#include "lwip/sockets.h"
#include "lwip/sys.h"
#include "lwip/init.h"
struct netif server_netif;

/* other includes. */
#include "platform_config.h"
#include "netif/xadapter.h"
#include "config_apps.h"

void print_headers();
void launch_app_threads();
void lwip_init();

void network_thread(void *p)
{
    struct netif *netif;
    struct ip_addr ipaddr, netmask, gw;

    /* the mac address of the board. this should be unique per board */
    unsigned char mac_ethernet_address[] = { 0x00, 0x0a, 0x35, 0x00, 0x01, 0x02 };

    xil_printf("Initialising lwip\r\n");
    lwip_init();
    netif = &server_netif;

    /* initliaze IP addresses to be used */
    IP4_ADDR(&ipaddr,  192, 168,   1, 10);
    IP4_ADDR(&netmask, 255, 255, 255,  0);
    IP4_ADDR(&gw,      192, 168,   1,  1);

    /* print out IP settings of the board */
    print("\n\n");
    print("----- Daedalus^RT demonstrator ------\r\n");

    print_ip_settings(&ipaddr, &netmask, &gw);
    /* print all application headers */

    /* Add network interface to the netif_list, and set it as default */
    xil_printf("Adding network interface\r\n");
    if (!xemac_add(netif, &ipaddr, &netmask, &gw, mac_ethernet_address, PLATFORM_EMAC_BASEADDR)) {
        xil_printf("Error adding N/W interface\r\n");
        return;
    }

    netif_set_default(netif);
    /* specify that the network if is up */
    netif_set_up(netif);

    xil_printf("Setting up input thread\r\n");
    sys_thread_new("xemacif_input_thread", (void(*)(void*))xemacif_input_thread, netif,THREAD_STACKSIZE,IO_THREADS_PRIO);


    print_headers();
    launch_app_threads();

#ifdef OS_IS_FREERTOS
    vTaskDelete(NULL);
#endif
    return;
}

void print_ip_settings(struct ip_addr *ip, struct ip_addr *mask, struct ip_addr *gw)
{

    print_ip("Board IP: ", ip);
    print_ip("Netmask : ", mask);
    print_ip("Gateway : ", gw);
}

void print_ip(char *msg, struct ip_addr *ip)
{
    print(msg);
    xil_printf("%d.%d.%d.%d\r\n", ip4_addr1(ip), ip4_addr2(ip),
            ip4_addr3(ip), ip4_addr4(ip));
}

#endif /* NETWORK_H_ */
