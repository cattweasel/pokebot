Ext.define('AuditEvent', {
    extend: 'Ext.data.Model',
    fields: [{
		name: 'id',
		type: 'string'
	}, {
		name: 'name',
		type: 'string'
    	}, {
    		name: 'source',
    		type: 'string'
    	}, {
    		name: 'target',
    		type: 'string'
    	}, {
    		name: 'created',
    		type: 'number'
    	}, {
    		name: 'modified',
    		type: 'number'
    	}]
});

Ext.define('User', {
    extend: 'Ext.data.Model',
    fields: [{
		name: 'id',
		type: 'string'
	}, {
		name: 'name',
		type: 'string'
    	}, {
    		name: 'firstname',
    		type: 'string'
    	}, {
    		name: 'lastname',
    		type: 'string'
    	}, {
    		name: 'username',
    		type: 'string'
    	}, {
    		name: 'languageCode',
    		type: 'string'
    	}, {
    		name: 'banned',
    		type: 'boolean'
    	}, {
    		name: 'created',
    		type: 'number'
    	}, {
    		name: 'modified',
    		type: 'number'
    	}]
});
