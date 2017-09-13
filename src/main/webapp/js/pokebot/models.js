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

Ext.define('BotSession', {
    extend: 'Ext.data.Model',
    fields: [{
		name: 'id',
		type: 'string'
	}, {
		name: 'name',
		type: 'string'
    	}, {
    		name: 'created',
    		type: 'number'
    	}, {
    		name: 'modified',
    		type: 'number'
    	}]
});

Ext.define('Profile', {
    extend: 'Ext.data.Model',
    fields: [{
		name: 'id',
		type: 'string'
	}, {
		name: 'name',
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

Ext.define('PokemonSetting', {
    extend: 'Ext.data.Model',
    fields: [{
		name: 'pokemonId',
		type: 'int'
	}, {
		name: 'displayName',
		type: 'string'
    	}, {
    		name: 'enabled',
    		type: 'boolean'
    	}, {
    		name: 'range',
    		type: 'integer'
    	}]
});
