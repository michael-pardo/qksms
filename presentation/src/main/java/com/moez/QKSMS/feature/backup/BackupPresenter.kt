/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.feature.backup

import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.common.util.BillingManager
import com.moez.QKSMS.repository.BackupRepository
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject

class BackupPresenter @Inject constructor(
        backupRepo: BackupRepository,
        private val billingManager: BillingManager,
        private val navigator: Navigator
) : QkPresenter<BackupView, BackupState>(BackupState()) {

    init {
        disposables += backupRepo.getBackups()
                .subscribe { backups -> newState { copy(lastBackup = backups.map { it.date }.max(), backups = backups) } }

        disposables += billingManager.upgradeStatus
                .subscribe { upgraded -> newState { copy(upgraded = upgraded) } }
    }

    override fun bindIntents(view: BackupView) {
        super.bindIntents(view)

        view.restoreClicks()
                .autoDisposable(view.scope())
                .subscribe { view.selectFile() }

        view.restoreFileSelected()
                .autoDisposable(view.scope())
                .subscribe()

        view.fabClicks()
                .withLatestFrom(billingManager.upgradeStatus) { _, upgraded -> upgraded }
                .autoDisposable(view.scope())
                .subscribe { upgraded ->
                    when (upgraded) {
                        true -> navigator.showQksmsPlusActivity("backup_fab")
                        false -> navigator.showQksmsPlusActivity("backup_fab")
                    }
                }
    }

}