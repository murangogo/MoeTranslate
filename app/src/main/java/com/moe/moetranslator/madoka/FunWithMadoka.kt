package com.moe.moetranslator.madoka

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentMadokaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live2dsdk.madoka.GLRenderer
import live2dsdk.madoka.LAppDelegate
import live2dsdk.madoka.LAppLive2DManager
import live2dsdk.madoka.Live2DCallbackCustom

class FunWithMadoka : Fragment() {
    private lateinit var binding: FragmentMadokaBinding
    private lateinit var viewModel: Live2DViewModel

    private lateinit var modelAdapter: Live2DModelAdapter
    private lateinit var expressionAdapter: Live2DExpressionAdapter
    private lateinit var motionAdapter: Live2DMotionAdapter

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { handleFolderSelection(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMadokaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGLSurfaceView()
        setupViewModel()
        setupAdapters()
        setupDrawers()
        setupClickListeners()
        observeData()

        viewModel.setCurrentModel("model_1")
        modelAdapter.setSelectedModel("model_1")
    }

    override fun onStart() {
        super.onStart()
        try {
            LAppDelegate.getInstance().onStart(requireActivity())
        } catch (e: Exception){
            showToast(getString(R.string.error_occurred, e.toString()))
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            binding.live2dContainer.onResume()
        } catch (e: Exception){
            showToast(getString(R.string.error_occurred, e.toString()))
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            binding.live2dContainer.onPause()
            LAppDelegate.getInstance().onPause()
        } catch (e: Exception){
            showToast(getString(R.string.error_occurred, e.toString()))
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            LAppDelegate.getInstance().onStop()
        } catch (e: Exception){
            showToast(getString(R.string.error_occurred, e.toString()))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LAppDelegate.getInstance().onDestroy()
        } catch (e: Exception){
            showToast(getString(R.string.error_occurred, e.toString()))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGLSurfaceView(){
        val glSurfaceView = binding.live2dContainer
        glSurfaceView.setEGLContextClientVersion(2) // OpenGL ES 2.0を利用

        val glRenderer = GLRenderer()
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        glSurfaceView.setOnTouchListener { view, event ->
                val pointX = event.x
                val pointY = event.y
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        LAppDelegate.getInstance().onTouchBegan(pointX, pointY)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        LAppDelegate.getInstance().onTouchEnd(pointX, pointY)
//                        view.performClick()
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        LAppDelegate.getInstance().onTouchMoved(pointX, pointY)
                        true
                    }
                    else -> {
                        false
                    }
                }
        }
    }

    private fun setupViewModel() {
        val database = ModelInfoRoomDatabase.getDatabase(requireContext())
        val repository = ModelInfoRepository(database.ModelInfoDAO())
        val fileUtil = Live2DFileUtil(requireContext())

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return Live2DViewModel(repository, fileUtil) as T
            }
        })[Live2DViewModel::class.java]
    }

    private fun setupAdapters() {
        modelAdapter = Live2DModelAdapter(
            onModelClick = { modelId ->
                val modelNumber = modelId.replace("model_", "").toInt()
                changeModel(modelNumber)
                viewModel.setCurrentModel(modelId)
                modelAdapter.setSelectedModel(modelId)
                binding.drawerLayout.closeDrawers()
            },
            onModelLongClick = { model ->
                showModelOptionsDialog(model)
            }
        )

        expressionAdapter = Live2DExpressionAdapter(
            onExpressionClick = { fileName ->
                displayExpression(fileName)
                binding.drawerLayout.closeDrawers()
            },
            onExpressionLongClick = { expression ->
                showRenameDialog(expression.id.toString(), expression.displayName) { newName ->
                    lifecycleScope.launch {
                        viewModel.updateExpressionName(expression.id, newName)
                    }
                }
            }
        )

        motionAdapter = Live2DMotionAdapter(
            onMotionClick = { fileName ->
                displayMotion(fileName)
                binding.drawerLayout.closeDrawers()
            },
            onMotionLongClick = { motion ->
                showRenameDialog(motion.id.toString(), motion.displayName) { newName ->
                    lifecycleScope.launch {
                        viewModel.updateMotionName(motion.id, newName)
                    }
                }
            }
        )
    }

    private fun setupDrawers() {
        // 创建分割线
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        ).apply {
            setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)!!)
        }

        // 为模型列表设置
        binding.modelsList.apply {
            adapter = modelAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(dividerItemDecoration)
        }

        // 为表情/动作列表设置
        binding.expressionsMotionsList.apply {
            adapter = expressionAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(dividerItemDecoration)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnModelList.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.END)
            }

            btnImportModel.setOnClickListener {
                showImportModelDialog()
            }

            btnExpressions.setOnClickListener {
                // 打开左侧抽屉
                drawerLayout.openDrawer(GravityCompat.START)
                // 切换到表情列表
                expressionsMotionsList.apply {
                    adapter = expressionAdapter
                }
            }

            btnMotions.setOnClickListener {
                // 打开左侧抽屉
                drawerLayout.openDrawer(GravityCompat.START)
                // 切换到动作列表
                expressionsMotionsList.apply {
                    adapter = motionAdapter
                }
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allModels.collect { models ->
                        modelAdapter.submitList(models)
                    }
                }

                launch {
                    viewModel.currentExpressions.collect { expressions ->
                        expressionAdapter.submitList(expressions)
                    }
                }

                launch {
                    viewModel.currentMotions.collect { motions ->
                        motionAdapter.submitList(motions)
                    }
                }
            }
        }
    }

    private fun showModelOptionsDialog(model: Live2DModel) {

        val options = if (model.modelId == "model_1") {
            arrayOf(getString(R.string.rename))
        } else {
            arrayOf(getString(R.string.rename), getString(R.string.delete_models))
        }

        AlertDialog.Builder(requireContext())
            .setTitle(model.displayName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameDialog(model.modelId, model.displayName) { newName ->
                        lifecycleScope.launch {
                            viewModel.updateModelName(model.modelId, newName)
                        }
                    }
                    1 -> showDeleteConfirmDialog(model)
                }
            }
            .show()
            .window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showRenameDialog(id: String, currentName: String, onConfirm: (String) -> Unit) {
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_message_edittext, null)
        customView.findViewById<TextView>(R.id.dialog_top_message).apply {
            text = getString(R.string.rename_tip)
        }
        val input = customView.findViewById<EditText>(R.id.dialog_bottom_edittext).apply {
            setText(currentName)
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.rename)
            .setView(customView)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotBlank()) {
                    onConfirm(newName)
                }
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showDeleteConfirmDialog(model: Live2DModel) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_deletion)
            .setMessage(getString(R.string.delete_description, model.displayName))
            .setPositiveButton(R.string.confirm) { _, _ ->
                // 显示进度对话框
                val progressDialog = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.waiting)
                    .setMessage(R.string.deleteing)
                    .setCancelable(false)
                    .create()

                progressDialog.show()
                progressDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

                lifecycleScope.launch {
                    try {
                        val success = viewModel.deleteModel(model.modelId)
                        progressDialog.dismiss()

                        if (success) {
                            showToast(getString(R.string.delete_success))
                        } else {
                            throw Exception("Delete Failed Exception.")
                        }
                    } catch (e: Exception) {
                        progressDialog.dismiss()
                        showToast(getString(R.string.delete_failed, e.message))
                    }
                }
            }
            .setNegativeButton(R.string.user_cancel, null)
            .show()
    }

    private fun showImportModelDialog() {

        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_message_edittext, null)
        customView.findViewById<TextView>(R.id.dialog_top_message).apply {
            text = getText(R.string.import_model_folder_message)
        }
        val input = customView.findViewById<EditText>(R.id.dialog_bottom_edittext).apply {
            hint = getString(R.string.model_name)
        }


        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.import_model_dialog_title)
            .setView(customView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val modelName = input.text.toString().trim()
                if (modelName.isNotBlank()) {
                    pendingModelName = modelName
                    pickFolderLauncher.launch(null)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.view_tutorial){_,_->
                val urlt = "https://blog.csdn.net/qq_45487246/article/details/131876712"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(urlt)
                startActivity(intent)
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private var pendingModelName: String? = null

    private fun handleFolderSelection(uri: Uri) {
        val modelName = pendingModelName ?: return
        pendingModelName = null

        // 显示进度对话框
        val progressDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("R.string.importing_model_title")
            .setMessage("R.string.importing_model_message")
            .setCancelable(false)
            .create()

        progressDialog.show()
        progressDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        lifecycleScope.launch {
            try {
                Log.d("FunWithMadoka", "uri:$uri \nImporting model with name: $modelName")
                val success = viewModel.importModel(uri, modelName)
                progressDialog.dismiss()

                val messageResId = if (success) {
                    "R.string.import_model_success"
                } else {
                    "R.string.import_model_failed"
                }
                Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                progressDialog.dismiss()
                Toast.makeText(context, "R.string.import_model_failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeModel(n: Int){
        val progressDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.loading_model)
            .setMessage(R.string.waiting)
            .setCancelable(false)
            .create()

        progressDialog.show()
        progressDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        LAppDelegate.getInstance().view.setChangeModel(n, object : Live2DCallbackCustom {
            override fun onSuccess() {
                lifecycleScope.launch(Dispatchers.Main) {
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(errorMessage: String?) {
                lifecycleScope.launch(Dispatchers.Main) {
                    progressDialog.dismiss()
                    showToast(getString(R.string.model_load_failed, errorMessage),true)
                }
            }
        })
    }

    private fun displayExpression(s: String){
        Log.d("FunWithMadoka", "Expression: $s")
        LAppLive2DManager.getInstance().getModel(0).setExpression(s)
    }

    private fun displayMotion(s: String){
        Log.d("FunWithMadoka", "Motion: $s")
        LAppLive2DManager.getInstance().getModel(0).startMotionCustom(s, null, null)
    }

    private fun showToast(str: String, isShort: Boolean = false) {
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }
}