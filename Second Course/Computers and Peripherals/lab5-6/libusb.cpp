#include <iostream>
#include <libusb.h>

using namespace std;

void printdev(libusb_device* dev);
string get_class_dev(uint8_t code);

int main() {
	libusb_device** devs;		// указатель на указатель на устройство,
								// используется для получения списка устройств
	libusb_context* ctx = NULL;	// контекст сессии libusb
	int r;						// для возвращаемых значений
	ssize_t cnt;				// число найденных USB-устройств
	ssize_t i;					// индексная переменная цикла перебора всех устройств
	// инициализировать библиотеку libusb, открыть сессию работы с libusb
	r = libusb_init(&ctx);
	if (r < 0) {
	    cerr << "Error: initialization failed: " << r << endl;
	    return 1;
	}

	// получить список всех найденных USB- устройств
	cnt = libusb_get_device_list(ctx, &devs);
	if (cnt < 0) {
	    cout << "Error: the list of USB-devices didn't get" << endl;
	    return 1;
	}

	cout << "Total number of connected devices: " << cnt << endl;
	cout << endl;

	for (i = 0; i < cnt; i++) 	// цикл перебора всех устройств
	    printdev(devs[i]);		// печать параметров устройства

	// освободить память, выделенную функцией получения списка устройств
	libusb_free_device_list(devs, 1);
	libusb_exit(ctx); 		// завершить работу с библиотекой libusb,

	return 0;
}

void printdev(libusb_device* dev) {
	cout << "===================================================|" << endl;
	libusb_device_descriptor desc; // дескриптор устройства
	libusb_config_descriptor* config; // дескриптор конфигурации объекта
	const libusb_interface* inter;
	const libusb_interface_descriptor* interdesc;
	const libusb_endpoint_descriptor* epdesc;
	int r = libusb_get_device_descriptor(dev, &desc);

	if (r < 0) {
	    cerr << "Error: device descriotor failed: " << r << endl;
	    return;
	}

	// получить конфигурацию устройства
	libusb_get_config_descriptor(dev, 0, &config);

	cout << get_class_dev((int)desc.bDeviceClass) << " - code: " << (int)desc.bDeviceClass << endl;
	cout << "Number of connected diveces: " << (int)desc.bNumConfigurations << endl;
	cout << "Vendor's ID: " << desc.idVendor << endl;
	cout << "Product ID: " << desc.idProduct << endl << endl;

	libusb_device_handle *handle;
	int openResult = libusb_open(dev, &handle);
	if (openResult == 0) {
        char buffer[256];

        if (desc.iProduct > 0 && libusb_get_string_descriptor_ascii(handle, desc.iProduct, reinterpret_cast<unsigned char*>(buffer), sizeof(buffer)) > 0)
			cout << "Product name: " << buffer << endl;
	    else
			cout << "Empty product name" << endl;

        if (desc.iManufacturer > 0 && libusb_get_string_descriptor_ascii(handle, desc.iManufacturer, reinterpret_cast<unsigned char*>(buffer), sizeof(buffer)) > 0)
			cout << "Vednor's name: " << buffer << endl;
        else
			cout << "Empty vendor's name" << endl;

        if (desc.iSerialNumber > 0 && libusb_get_string_descriptor_ascii(handle, desc.iSerialNumber, reinterpret_cast<unsigned char*>(buffer), sizeof(buffer)) > 0)
			cout << "Serial number: " << buffer << endl;
        else
			cout << "Empty serial number" << endl;

        libusb_close(handle);
	}
	else
        cerr << "The device couldn't be open to get additional info" << endl;

	cout << endl << "Number of interfaces: " << (int)config->bNumInterfaces << endl;

	for (int i = 0; i < (int)config->bNumInterfaces; i++) {
		inter = &config->interface[i];
		cout << "---------------------------------------------------|" << endl;
		cout << "Number of alternative settings: " << inter->num_altsetting << endl;

		for (int j = 0; j < inter->num_altsetting; j++) {
			interdesc = &inter->altsetting[j];
			cout << "Interface number: " << (int)interdesc->bInterfaceNumber << endl;
			cout << "Number of end points: " << (int)interdesc->bNumEndpoints << endl;

			for (int k = 0; k < (int)interdesc->bNumEndpoints; k++) {
				epdesc = &interdesc->endpoint[k];
				cout << "Descriptor type: " << (int)epdesc->bDescriptorType << endl;
				cout << "Adress of end point: " << (int)(int)epdesc->bEndpointAddress << endl;
			}
			if (j != (inter->num_altsetting - 1)) cout << endl;
		}
	}
	libusb_free_config_descriptor(config);

	cout << "===================================================|" << endl << endl;
}

string get_class_dev(uint8_t code) {
	switch (code) {
	    case 0x00: return "Code is missing";
	    case 0x01: return "Audio device";
	    case 0x02: return "Network adapter";
	    case 0x03: return "UI device";
	    case 0x05: return "Physical device";
	    case 0x06: return "Image";
	    case 0x07: return "Printer";
	    case 0x08: return "Storage device";
	    case 0x09: return "Hub";
	    case 0x0A: return "CDC-Data";
	    case 0x0B: return "Smart Card";
	    case 0x0D: return "Content Security";
	    case 0x0E: return "Video Device";
	    case 0x0F: return "Personal medical device";
	    case 0x10: return "Audio and Video devices";
	    case 0xDC: return "Diagnostic device";
	    case 0xE0: return "Wireless controller";
	    case 0xEF: return "Different device";
	    case 0xFE: return "Specific device";
	    default: return "Unknown device";
	}
}
