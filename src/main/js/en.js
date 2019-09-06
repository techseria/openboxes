const en = {
  navbar: {
    dashboard: {
      label: 'Dashboard',
      link: `${process.env.REACT_APP_BASE_NAME}/dashboard/index`,
    },
    analytics: {
      label: 'Analytics',
      link: '#',
      adminOnly: true,
      subsections: {
        inventoryBrowser: {
          label: 'Browse Inventory',
          link: `${process.env.REACT_APP_BASE_NAME}/inventoryBrowser/index`,
        },
        snapshot: {
          label: 'Inventory Snapshots',
          link: `${process.env.REACT_APP_BASE_NAME}/snapshot/list`,
        },
      },
    },
    inventory: {
      label: 'Inventory',
      link: '#',
      activity: ['MANAGE_INVENTORY'],
      subsections: {
        browse: {
          label: 'Browse Inventory',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?resetSearch=true`,
        },
        manage: {
          label: 'Manage Inventory',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/manage`,
        },
        browseByCategory: {
          label: 'Browse by Category',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?resetSearch=true`,
        },
        chemicals: {
          label: 'Chemicals',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?subcategoryId=C0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on`,
        },
        drugs: {
          label: 'Drugs',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?subcategoryId=D0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on`,
        },
        facilities: {
          label: 'Facilities',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?subcategoryId=F0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on`,
        },
        itAndCommunicationsEq: {
          label: 'IT & Communications Equipment',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?subcategoryId=I0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on`,
        },
        lab: {
          label: 'Lab',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?subcategoryId=L0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on`,
        },
        medEquipSupply: {
          label: 'MedEquipSupply',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?subcategoryId=M0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on`,
        },
        vehiclesAndParts: {
          label: 'Vehicles and Parts',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/browse?subcategoryId=V0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on`,
        },
      },
    },
    orders: {
      label: 'Orders',
      link: '#',
      activity: ['PLACE_ORDER', 'FULFILL_ORDER'],
      subsections: {
        purchaseOrdersWorkflow: {
          label: 'Create order',
          link: `${process.env.REACT_APP_BASE_NAME}/purchaseOrderWorkflow/index`,
        },
        orders: {
          label: 'Order',
          link: `${process.env.REACT_APP_BASE_NAME}/order/list?orderTypeCode=PURCHASE_ORDER`,
        },
        completed: {
          label: 'Completed',
          link: `${process.env.REACT_APP_BASE_NAME}/order/list?status=COMPLETED`,
        },
        placed: {
          label: 'Placed',
          link: `${process.env.REACT_APP_BASE_NAME}/order/list?status=PLACED`,
        },
      },
    },
    requisitions: {
      label: 'Requisitions',
      link: '#',
      activity: ['PLACE_REQUEST', 'FULFILL_REQUEST'],
      subsections: {
        stockRequisition: {
          label: 'Create stock requisition',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/chooseTemplate?type=STOCK`,
        },
        nonStockRequisition: {
          label: 'Create non-stock requisition',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/create?type=NON_STOCK`,
        },
        adHocRequisition: {
          label: 'Create adhoc stock requisition',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/create?type=ADHOC`,
        },
        requisitionList: {
          label: 'Requisitions',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/list`,
        },
        requisitionsAll: {
          label: 'All',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/list`,
        },
        requisitionsCreated: {
          label: 'Created',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/list?status=CREATED`,
        },
        requisitionsChecking: {
          label: 'Checking',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/list?status=CHECKING`,
        },
        requisitionsIssued: {
          label: 'Issued',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/list?status=ISSUED`,
        },
      },
    },
    inbound: {
      label: 'Inbound',
      link: '#',
      activity: ['RECEIVE_STOCK'],
      subsections: {
        create: {
          configName: 'stockMovement',
          label: 'Create Inbound Stock Movement',
          link: `${process.env.REACT_APP_BASE_NAME}/stockMovement/create?direction=INBOUND`,
        },
        list: {
          configName: 'stockMovement',
          label: 'List Inbound Stock Movements',
          link: `${process.env.REACT_APP_BASE_NAME}/stockMovement/list?direction=INBOUND`,
        },
        createPutAway: {
          configName: 'stockMovement',
          label: 'Create Putaway',
          link: `${process.env.REACT_APP_BASE_NAME}/putAway/index`,
        },
        listPutAways: {
          configName: 'stockMovement',
          label: 'List Putaways',
          link: `${process.env.REACT_APP_BASE_NAME}/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING`,
        },
        createShipment: {
          configName: 'receiving',
          label: 'Create Inbound Shipment',
          link: `${process.env.REACT_APP_BASE_NAME}/createShipmentWorkflow/createShipment?type=INCOMING`,
        },
        listShipments: {
          configName: 'receiving',
          label: 'Inbound Shipments',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=incoming`,
        },
        all: {
          configName: 'receiving',
          label: 'All',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=incoming`,
        },
        receiving: {
          configName: 'receiving',
          label: 'Receiving',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=incoming&status=PARTIALLY_RECEIVED`,
        },
        pending: {
          configName: 'receiving',
          label: 'Pending',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=incoming&status=PENDING`,
        },
        received: {
          configName: 'receiving',
          label: 'Received',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=incoming&status=RECEIVED`,
        },
        shipped: {
          configName: 'receiving',
          label: 'Shipped',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=incoming&status=SHIPPED`,
        },
      },
    },
    outbound: {
      label: 'Outbound',
      link: '#',
      activity: ['SEND_STOCK'],
      subsections: {
        create: {
          configName: 'stockMovement',
          label: 'Create Outbound Stock Movement',
          link: `${process.env.REACT_APP_BASE_NAME}/stockMovement/create?direction=OUTBOUND`,
        },
        list: {
          configName: 'stockMovement',
          label: 'List Outbound Stock Movements',
          link: `${process.env.REACT_APP_BASE_NAME}/stockMovement/list?direction=OUTBOUND`,
        },
        createShipment: {
          configName: 'shipping',
          label: 'Create Outbound Shipment',
          link: `${process.env.REACT_APP_BASE_NAME}/createShipmentWorkflow/createShipment?type=OUTGOING`,
        },
        listShipments: {
          configName: 'shipping',
          label: 'Outbound Shipments',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=outgoing`,
        },
        all: {
          configName: 'shipping',
          label: 'All',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?type=outgoing`,
        },
        pending: {
          configName: 'shipping',
          label: 'Pending',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?status=PENDING`,
        },
        received: {
          configName: 'shipping',
          label: 'Received',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?status=RECEIVED`,
        },
        shipped: {
          configName: 'shipping',
          label: 'Shipped',
          link: `${process.env.REACT_APP_BASE_NAME}/shipment/list?status=SHIPPED`,
        },
      },
    },
    reporting: {
      label: 'Reporting',
      link: '#',
      subsections: {
        cycleCountReport: {
          label: 'Cycle Count Report',
          link: `${process.env.REACT_APP_BASE_NAME}/cycleCount/exportAsCsv`,
        },
        showBinLocationReport: {
          label: 'Bin Location Report',
          link: `${process.env.REACT_APP_BASE_NAME}/report/showBinLocationReport`,
        },
        inventory: {
          label: 'Baseline QoH Report',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/show`,
        },
        showTransactionReport: {
          label: 'Transcription Report',
          link: `${process.env.REACT_APP_BASE_NAME}/report/showTransactionReport`,
        },
        consumption: {
          label: 'Consumption Report',
          link: `${process.env.REACT_APP_BASE_NAME}/consumption/show`,
        },
        listDailyTransactions: {
          label: 'Daily Transactions Report',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/listDailyTransactions`,
        },
        showShippingReport: {
          label: 'Shipping Report',
          link: `${process.env.REACT_APP_BASE_NAME}/report/showShippingReport`,
        },
        showInventorySamplingReport: {
          label: 'Inventory Sampling Report',
          link: `${process.env.REACT_APP_BASE_NAME}/report/showInventorySamplingReport`,
        },
        listExpiredStock: {
          label: 'Expired stock',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/listExpiredStock`,
        },
        listExpiringStock: {
          label: 'Expiring stock',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/listExpiringStock`,
        },
        listLowStock: {
          label: 'Low stock',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/listLowStock`,
        },
        listReorderStock: {
          label: 'Reorder stock',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/listReorderStock`,
        },
        exportBinLocation: {
          label: 'Export bin locations',
          link: `${process.env.REACT_APP_BASE_NAME}/report/exportBinLocation?downloadFormat=csv`,
        },
        showInventoryByLocationReport: {
          label: 'Inventory By Location Report',
          link: `${process.env.REACT_APP_BASE_NAME}/report/showInventoryByLocationReport`,
        },
        exportAsCsv: {
          label: 'Export products',
          link: `${process.env.REACT_APP_BASE_NAME}/product/exportAsCsv`,
        },
        exportLatestInventoryDate: {
          label: 'Export latest inventory date',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/exportLatestInventoryDate`,
        },
        inventoryLevelExport: {
          label: 'Export inventory levels',
          link: `${process.env.REACT_APP_BASE_NAME}/inventoryLevel/export`,
        },
        requisitionExport: {
          label: 'Export requisitions',
          link: `${process.env.REACT_APP_BASE_NAME}/requisition/export`,
        },
        requisitionItem: {
          label: 'Export requisition intems',
          link: `${process.env.REACT_APP_BASE_NAME}/requisitionItem/listCanceled`,
        },
      },
    },
    products: {
      label: 'Products',
      link: '#',
      activity: ['MANAGE_INVENTORY'],
      subsections: {
        product: {
          label: 'Products',
          link: `${process.env.REACT_APP_BASE_NAME}/product/list`,
        },
        productGroup: {
          label: 'Generic Products',
          link: `${process.env.REACT_APP_BASE_NAME}/productGroup/list`,
        },
        productSupplier: {
          label: 'Products Suppliers',
          link: `${process.env.REACT_APP_BASE_NAME}/productSupplier/list`,
        },
        productAssociation: {
          label: 'Associations',
          link: `${process.env.REACT_APP_BASE_NAME}/productAssociation/list`,
        },
        productCatalog: {
          label: 'Catalogs',
          link: `${process.env.REACT_APP_BASE_NAME}/productCatalog/list`,
        },
        productComponent: {
          label: 'Components',
          link: `${process.env.REACT_APP_BASE_NAME}/productComponent/list`,
        },
        attribute: {
          label: 'Attributes',
          link: `${process.env.REACT_APP_BASE_NAME}/attribute/list`,
        },
        category: {
          label: 'Categories',
          link: `${process.env.REACT_APP_BASE_NAME}/category/tree`,
        },
        tag: {
          label: 'Tags',
          link: `${process.env.REACT_APP_BASE_NAME}/tag/list`,
        },
        unitOfMeasure: {
          label: 'Unit of Measure',
          link: `${process.env.REACT_APP_BASE_NAME}/unitOfMeasure/list`,
        },
        unitOfMeasureClass: {
          label: 'UoM Class',
          link: `${process.env.REACT_APP_BASE_NAME}/unitOfMeasureClass/list`,
        },
        inventoryLevel: {
          label: 'Inventory Levels',
          link: `${process.env.REACT_APP_BASE_NAME}/inventoryLevel/list`,
        },
        productCreate: {
          label: 'Create new product',
          link: `${process.env.REACT_APP_BASE_NAME}/product/create`,
          adminOnly: true,
        },
        productBatchEdit: {
          label: 'Batch edit product',
          link: `${process.env.REACT_APP_BASE_NAME}/product/batchEdit`,
          adminOnly: true,
        },
        productImportAsCsv: {
          label: 'import products',
          link: `${process.env.REACT_APP_BASE_NAME}/product/importAsCsv`,
          adminOnly: true,
        },
        productExportAsCsv: {
          label: 'Export products',
          link: `${process.env.REACT_APP_BASE_NAME}/product/exportAsCsv`,
          adminOnly: true,
        },
      },
    },
    requisitionTemplate: {
      label: 'Stock lists',
      link: `${process.env.REACT_APP_BASE_NAME}/requisitionTemplate/list`,
    },
    configuration: {
      label: 'Configuration',
      link: '#',
      adminOnly: true,
      subsections: {
        showSettings: {
          label: 'Settings',
          link: `${process.env.REACT_APP_BASE_NAME}/admin/showSettings`,
        },
        migration: {
          label: 'Migrate Data',
          link: `${process.env.REACT_APP_BASE_NAME}/migration/index`,
        },
        console: {
          label: 'Console',
          link: `${process.env.REACT_APP_BASE_NAME}/console/index`,
        },
        cache: {
          label: 'Cache',
          link: `${process.env.REACT_APP_BASE_NAME}/admin/cache`,
        },
        sendMail: {
          label: 'Email',
          link: `${process.env.REACT_APP_BASE_NAME}/admin/sendMail`,
        },
        localization: {
          label: 'Localization',
          link: `${process.env.REACT_APP_BASE_NAME}/localization/list`,
        },
        documentType: {
          label: 'Document Types',
          link: `${process.env.REACT_APP_BASE_NAME}/documentType/list`,
        },
        eventType: {
          label: 'Event Types',
          link: `${process.env.REACT_APP_BASE_NAME}/eventType/list`,
        },
        locationGroup: {
          label: 'Location groups',
          link: `${process.env.REACT_APP_BASE_NAME}/locationGroup/list`,
        },
        locationType: {
          label: 'Location types',
          link: `${process.env.REACT_APP_BASE_NAME}/locationType/list`,
        },
        partyType: {
          label: 'Party types',
          link: `${process.env.REACT_APP_BASE_NAME}/partyType/list`,
        },
        partyRole: {
          label: 'Party roles',
          link: `${process.env.REACT_APP_BASE_NAME}/partyRole/list`,
        },
        location: {
          label: 'Locations',
          link: `${process.env.REACT_APP_BASE_NAME}/location/list`,
        },
        shipper: {
          label: 'Shippers',
          link: `${process.env.REACT_APP_BASE_NAME}/shipper/list`,
        },
        organization: {
          label: 'Organizations',
          link: `${process.env.REACT_APP_BASE_NAME}/organization/list`,
        },
        shipmentWorkflow: {
          label: 'Shipment Workflows',
          link: `${process.env.REACT_APP_BASE_NAME}/shipmentWorkflow/list`,
        },
        document: {
          label: 'Documents',
          link: `${process.env.REACT_APP_BASE_NAME}/document/list`,
        },
        person: {
          label: 'People',
          link: `${process.env.REACT_APP_BASE_NAME}/person/list`,
        },
        listAllTransactions: {
          label: 'Transactions',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/listAllTransactions`,
        },
        user: {
          label: 'Users',
          link: `${process.env.REACT_APP_BASE_NAME}/user/list`,
        },
        editTransaction: {
          label: 'Add transaction',
          link: `${process.env.REACT_APP_BASE_NAME}/inventory/editTransaction`,
        },
        importInventory: {
          label: 'Import Inventory',
          link: `${process.env.REACT_APP_BASE_NAME}/batch/importData?type=inventory`,
        },
        importInventoryLevel: {
          label: 'Import Inventory Level',
          link: `${process.env.REACT_APP_BASE_NAME}/batch/importData?type=inventoryLevel`,
        },
      },
    },
    customLinks: {
      label: 'Custom Links',
      link: '#',
      renderedFromConfig: true,
    },
  },
};

export default en;
