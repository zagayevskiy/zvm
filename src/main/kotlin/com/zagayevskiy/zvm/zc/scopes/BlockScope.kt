package com.zagayevskiy.zvm.zc.scopes

interface BlockScope: LocalScope

class BlockScopeDelegate(enclosingScope: Scope?): BlockScope, BaseLocalScope(enclosingScope)